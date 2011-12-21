/**
 * Copyright 2011 Jason Ferguson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jason.heasarcutils.catalogparser.util.io;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.jason.heasarcutils.catalogparser.util.Catalog;
import org.jason.heasarcutils.catalogparser.util.FieldData;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class DataImporter {

    private EventBus eventBus;

    @Inject
    public DataImporter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Single point of entry for the Importer
     *
     * @param catalog Catalog object representing to astronomical catalog to process into JSON
     */

    public void processFile(Catalog catalog) throws IOException {

        String fileUrl = catalog.getUrl();
        BufferedReader reader = null;
        BufferedWriter writer = null;

        // set up a context to determine if we are processing a TDAT or DAT. Yay strategy pattern!
        Context context;
        if (catalog.getType().equalsIgnoreCase("tdat")) {
            context = new Context(new TdatStrategy(catalog));
        } else {
            context = new Context(new DatStrategy(catalog));
        }
        try {
            // technically we could check for GZ, ZIP, or uncompressed files here.
            if (isGzipFile(fileUrl)) {
                reader = createGzipReader(fileUrl);
            }
            writer = getWriter(catalog.getName());

            while (reader.ready()) {
                String line = reader.readLine();
                Map<String, String> data = context.processLine(line);
                data = filterResults(data, catalog);
                String jsonLine = getJsonLine(data);
                writer.write(jsonLine);
            }
        } finally {
            closeQuietly(reader);
            closeQuietly(writer);
        }

    }

    private Map<String, String> filterResults(Map<String, String> results, Catalog catalog) {

        results = removeNulls(results);
        results = removeUnwantedFields(results, catalog);
        results = fixFieldPrefixes(results, catalog);
        results = fixFieldNames(results, catalog);

        return results;
    }

    private boolean isGzipFile(String filename) {
        return filename.matches("\\.gz^");
    }

    private BufferedReader createGzipReader(String fileUrl) throws IOException {

        GZIPInputStream gzis = new GZIPInputStream(createInputStream(fileUrl));
        InputStreamReader isr = new InputStreamReader(gzis, "UTF-8");

        return new BufferedReader(isr);
    }

    private InputStream createInputStream(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return url.openStream();
    }

    private BufferedWriter getWriter(String catalogName) throws IOException {

        FileWriter writer = new FileWriter(catalogName + ".json");
        return new BufferedWriter(writer);
    }

    protected String getFilename(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length() - 1);
    }

    private Map<String, String> removeNulls(Map<String, String> map) {

        Map<String, String> result = new LinkedHashMap<String, String>();

        for (String key : map.keySet()) {
            if (map.get(key) != null && map.get(key).length() > 0) {
                result.put(key, map.get(key));
            }
        }

        return result;
    }

    private Map<String, String> removeUnwantedFields(Map<String, String> data, Catalog catalog) {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : catalog.getFieldData().keySet()) {
            FieldData fd = catalog.getFieldData().get(key);
            if (fd.isIncluded()) {
                if (data.get(key) != null) {
                    result.put(key, data.get(key));
                }
            }

        }
        return result;
    }

    /**
     * Determine if the field name needs to be prefixed and do so if necessary
     *
     * @param data
     * @param catalog
     * @return
     */
    private Map<String, String> fixFieldPrefixes(Map<String, String> data, Catalog catalog) {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : catalog.getFieldData().keySet()) {
            FieldData fd = catalog.getFieldData().get(key);
            if (fd.isIncluded()) {
                if (data.get(key) != null) {
                    if (fd.getPrefix() != null && data.get(key).contains(fd.getPrefix())) {
                        result.put(key, fd.getPrefix() + data.get(key));
                    } else {
                        result.put(key, data.get(key));
                    }
                }
            }

        }

        return result;
    }

    /**
     * Determine if the field needs to be renamed and fix it if necessary
     *
     * @param data
     * @param catalog
     * @return
     */
    private Map<String, String> fixFieldNames(Map<String, String> data, Catalog catalog) {
        // Set result to be the input value, we'll remove values rather than add
        Map<String, String> result = data;
        // loop through the map of field data for the catalog configuration
        for (String key : catalog.getFieldData().keySet()) {
            // get the field data for the field identified by the key
            FieldData fd = catalog.getFieldData().get(key);

            // get the value to rename to
            String renameValue = fd.getRenameTo();

            // if there is a value to rename to, copy it to a new key representing the renamed value
            if (renameValue != null && renameValue.length() > 0) {
                // this is kind of a big-hammer approach, but I don't want to write null values to the
                // map containing the data, which may be returned from the following statement
                if (result.get(key) == null) {
                    continue;
                }

                result.put(renameValue, data.get(key));
                // if we don't keep it after the rename, drop the key
                if (!fd.isKeepAfterCopy()) {
                    result.remove(key);
                }
            }
        }

        return result;
    }

    private String getJsonLine(Map<String, String> data) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (String key : data.keySet()) {
            sb.append(key);
            sb.append(":");
            if (isNumber(data.get(key))) {
                if (isInteger(data.get(key))) {
                    sb.append(new Integer(data.get(key).trim()));
                } else {
                    BigDecimal number = new BigDecimal(data.get(key).trim());
                    number = number.setScale(4, BigDecimal.ROUND_HALF_EVEN);
                    sb.append(number);
                }
            } else {
                sb.append("\"");
                sb.append(data.get(key));
                sb.append("\"");
            }
            sb.append(",");
        }
        sb = new StringBuffer(sb.substring(0, sb.length() - 1)); // stupid trailing comma
        sb.append("}\r\n");

        return sb.toString();
    }

    private boolean isInteger(String value) {
        if (value == null) {
            return false;
        }
        String pattern = "^\\s*[\\+,-]?[0-9]+$";
        return value.matches(pattern);
    }

    private boolean isDouble(String value) {
        if (value == null) {
            return false;
        }
        String pattern = "^\\s*[\\+,-]?[0-9]*\\.[0-9]*$";
        return value.matches(pattern);
    }

    private boolean isNumber(String value) {
        return (isInteger(value) || isDouble(value));
    }

    /**
     * Attempt to implement Strategy design pattern to determine whether to process a line of input
     * as being from a DAT or a TDAT file (or potentially something entirely separate)
     */
    public interface Strategy {

        /**
         * Process a line returned from a file
         *
         * @return a Map<String, String> containing data read from the file matched to it's key
         */
        public Map<String, String> processLine(String line);
    }

    /**
     * Holder class for the strategy
     */
    public class Context {

        private Strategy strategy;

        public Context(Strategy strategy) {
            this.strategy = strategy;
        }

        public Map<String, String> processLine(String line) {
            return strategy.processLine(line);
        }
    }

    /**
     * Implementation of Strategy to deal with TDAT files
     */
    public class TdatStrategy implements Strategy {

        private Catalog catalog;

        public TdatStrategy(Catalog catalog) {
            this.catalog = catalog;
        }

        @Override
        public Map<String, String> processLine(String line) {
            if (!line.matches("^(.*?\\|)*$")) {
                return null;
            }
            Map<String, String> result = new HashMap<String, String>();
            String[] fieldNames = catalog.getFieldData().keySet().toArray(new String[]{});
            String[] fieldValues = line.split("\\|");

            for (int i = 0; i < fieldValues.length; i++) {
                FieldData fd = catalog.getFieldData().get(fieldNames[i]);
                if (catalog.getFieldDataSet().contains(fd)) {
                    result.put(fieldNames[i], fieldValues[i]);
                }
            }

            return result;
        }
    }

    /**
     * Implementation of Strategy to deal with DAT files
     */
    public class DatStrategy implements Strategy {

        private Catalog catalog;
        private Map<String, String> template;

        public DatStrategy(Catalog catalog) {
            this.catalog = catalog;

            // set up the template
            this.template = new LinkedHashMap<String, String>(catalog.getFieldData().size());
            for (String fieldName : catalog.getFieldData().keySet()) {
                template.put(fieldName, null);
            }
        }

        @Override
        public Map<String, String> processLine(String line) {

            Map<String, String> result = template;
            for (String key : catalog.getFieldData().keySet()) {
                FieldData fd = catalog.getFieldData().get(key);
                if (fd.getPrefix() != null) {
                    result.put(fd.getRenameTo(), line.substring(fd.getStart() - 1, fd.getEnd()).trim());
                } else {
                    result.put(key, line.substring(fd.getStart() - 1, fd.getEnd()).trim());
                }
            }

            return result;
        }
    }
}
