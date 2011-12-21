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
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jason.heasarcutils.catalogparser.ui.event.ProcessCatalogEvent;
import org.jason.heasarcutils.catalogparser.ui.event.statusBar.SetStatusBarTextEvent;
import org.jason.heasarcutils.catalogparser.util.Catalog;
import org.jason.heasarcutils.catalogparser.util.FieldData;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Utility class to manage the application data
 *
 * The only way this class should be accessed is via its eventhandler methods, which are annotated
 * with the Guava @Subscribe annotation. This class is also configured to be an eager singleton in
 * the Application Module so that it is initialized once at startup.
 *
 * This class is also my first attempt at implementing a Strategy pattern. The processing for
 * TDAT and DAT files is slightly different, so I created the following:
 *
 * - Strategy - interface for different implementations of processLine(String)
 * - Context - holder class for the strategy
 * - DatStrategy - implementation of Strategy to process lines read from a DAT file
 * - TdatStrategy - implementation of Strategy to process lines read from a TDAT file
 *
 * @author Jason Ferguson
 * @since 0.2
 */
@Singleton
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DataManager {

    private EventBus eventBus;

    @Inject
    public DataManager(EventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.register(this);
    }

    /**
     * Event handler method, which starts the ball rolling when another class fires/posts a
     * ProcessCatalogEvent
     *
     * @param e     ProcessCatalogEvent class
     */
    @Subscribe
    public void processCatalog(ProcessCatalogEvent e) {

        Catalog catalog = e.getCatalog();
        try {
            eventBus.post(new SetStatusBarTextEvent("Importing " + catalog.getName()));
            processFile(catalog);
            eventBus.post(new SetStatusBarTextEvent("Completed import"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * "Top-level" method which configures the readers
     *
     * @param catalog Catalog object representing to astronomical catalog to process into JSON
     * @throws IOException  something went wrong when setting up the reader, writer, or URL
     */
    private void processFile(Catalog catalog) throws IOException {

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
                if (data == null) {
                    continue;
                }
                data = filterResults(data, catalog);
                String jsonLine = getJsonLine(data);
                writer.write(jsonLine);
            }
        } finally {
            closeQuietly(reader);
            closeQuietly(writer);
        }

    }

    /**
     * Method to process the Map of data (unwanted fields/nulls/prefixes/etc)
     *
     * @param results   Map<String, String> to process
     * @param catalog   Catalog telling how to process the data
     * @return  processed Map<String, String>
     */
    private Map<String, String> filterResults(Map<String, String> results, Catalog catalog) {

        results = removeNulls(results);
        results = removeUnwantedFields(results, catalog);
        results = fixFieldPrefixes(results, catalog);
        results = fixFieldNames(results, catalog);

        return results;
    }

    /**
     * Quick and dirty utility method to determine if the file is a gzip file
     *
     * @param filename  filename to check
     * @return  true if the extension is gz, false otherwise
     */
    private boolean isGzipFile(String filename) {
        int dot = filename.lastIndexOf(".");
        String extension = filename.substring(dot + 1);

        return extension.equalsIgnoreCase("gz");
    }

    /**
     * Create a Buffered Reader based on a GZipInputStream, since there isn't any sort of
     * GZipReader class)
     *
     * @param fileUrl   String representing the URL of the remote file
     * @return  a BufferedReader from the URL
     * @throws IOException  thrown when something goes wrong creating a reader
     */
    private BufferedReader createGzipReader(String fileUrl) throws IOException {

        GZIPInputStream gzis = new GZIPInputStream(createInputStream(fileUrl));
        InputStreamReader isr = new InputStreamReader(gzis, "UTF-8");

        return new BufferedReader(isr);
    }

    /**
     * Create an InputStream from a String representing a remote URL
     *
     * @param urlLocation   String representing the URL of the remote file
     * @return  an InputStream from the remote file
     * @throws IOException thrown when something goes wrong creating a reader
     */
    private InputStream createInputStream(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return url.openStream();
    }

    /**
     *
     * @param catalogName   name of catalog, used to determine output file name
     * @return  BufferedWriter to send output to
     * @throws IOException thrown when something goes wrong creating a writer
     */
    private BufferedWriter getWriter(String catalogName) throws IOException {

        FileWriter writer = new FileWriter(catalogName + ".json");
        return new BufferedWriter(writer);
    }

    /**
     * Remove null values from a map.
     * @param map   Map to remove null values from
     * @return  Map with null values removed
     */
    private Map<String, String> removeNulls(Map<String, String> map) {

        Map<String, String> result = new LinkedHashMap<String, String>();

        for (String key : map.keySet()) {
            if (map.get(key) != null && map.get(key).length() > 0) {
                result.put(key, map.get(key));
            }
        }

        return result;
    }

    /**
     * Remove unwanted fields from the map based on data from the catalog
     *
     * @param data  Map to remove unwanted key-value pairs from
     * @param catalog   Catalog telling how to process the data
     * @return  Map with the unwanted fields removed
     */
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
     * @param data      Map<String, String> to fix the field names for
     * @param catalog   Catalog telling how to process the data
     * @return      Map with prefixes added to field values, as determined by the Catalog object
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
     * @param data    Map<String, String> to process the field names for
     * @param catalog Catalog object stating how to process the names
     * @return Map<String,String> with renamed fields
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

    /**
     * Convert a Map<String, String> to a line of JSON. Determines if a value is a number and if so,
     * doesn't put quotes and rounds it to 4 decimal places.
     *
     * @param data  Map to convert to a JSON string
     * @return  String representing the Map in JSON form
     */
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

    /**
     * Quick and dirty method to determine if a String represents an integer. It's not too robust, but works
     * for what I need it for.
     *
     * @param value     String to check if is an integer
     * @return  true is value represents an integer, false otherwise
     */
    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean isInteger(String value) {
        if (value == null) {
            return false;
        }

        return Pattern.matches("^\\s*[\\+,-]?[0-9]+$", value);
//        String pattern = "^\\s*[\\+,-]?[0-9]+$";
//        return value.matches(pattern);
    }

    /**
     * Quick and dirty method to determine if a String represents a double. Not too robust, but works for
     * what I need
     *
     * @param value String to check
     * @return  true if value represents a double, false otherwise
     */
    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean isDouble(String value) {
        if (value == null) {
            return false;
        }

        return Pattern.matches("^\\s*[\\+,-]?[0-9]*\\.[0-9]*$", value);
//        String pattern = "^\\s*[\\+,-]?[0-9]*\\.[0-9]*$";
//        return value.matches(pattern);
    }

    /**
     * Quick and dirty method to determine if a string is any type of number, integer or double
     *
     * @param value String to check
     * @return  true if String represents a number, false otherwise
     */
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
         * @param line String representing a single line of data
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
            // make sure the line is a pipe-deliniated set of data
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
