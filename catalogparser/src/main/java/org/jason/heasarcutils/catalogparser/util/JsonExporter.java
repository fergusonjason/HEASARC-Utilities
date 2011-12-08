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
package org.jason.heasarcutils.catalogparser.util;

import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Class to handle the export to JSON
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class JsonExporter {

    private Catalog catalog;

    public JsonExporter(Catalog catalog) {
        this.catalog = catalog;
    }

    public void exportToJSON() {

        System.out.println("Exporting " + catalog.getName());
        if (catalog.getType().equals("tdat")) {
            // import tdat file
            String filename = catalog.getUrl();
            processTdatFile(filename);
        } else {
            // import dat file
            String filename = catalog.getUrl();
            processDatFile(filename);
        }

    }

    private void processTdatFile(String filename) {
        try {
            URL url = new URL(filename);

            // set up input
            GZIPInputStream gzis = new GZIPInputStream(url.openStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzis));

            // set up output
            BufferedWriter writer = new BufferedWriter(new FileWriter(catalog.getName() + ".json"));

            // create a template so I only have to create a map once
            Map<String, String> template = new LinkedHashMap<String, String>(catalog.getFieldData().size());
            for (String fieldName : catalog.getFieldData().keySet()) {
                template.put(fieldName, null);
            }

            // start processing
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.matches("^(.*?\\|)*$")) {
                    //String[] fieldNames = null;
                    Map<String, String> result = new HashMap<String, String>();
                    String[] fieldNames = (String[]) catalog.getFieldData().keySet().toArray();
                    String[] fieldValues = line.split("|");

                    for (int i=0; i<fieldNames.length; i++) {
                        result.put(fieldNames[i], fieldValues[i]);
                    }

                    result = removeNulls(result);
                    writer.write(getJsonLine(result));

                }
            }

            writer.close();
            reader.close();
            gzis.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processDatFile(String filename) {

        try {
            URL url = new URL(filename);
            // set up the data input
            GZIPInputStream gzis = new GZIPInputStream(url.openStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzis));

            // set up the data output
            BufferedWriter writer = new BufferedWriter(new FileWriter(catalog.getName() + ".json"));

            // create a template so I only have to create a map once
            Map<String, String> template = new LinkedHashMap<String, String>(catalog.getFieldData().size());
            for (String fieldName : catalog.getFieldData().keySet()) {
                template.put(fieldName, null);
            }

            while (reader.ready()) {
                String line = reader.readLine();
                Map<String, String> fieldMap = template;
                for (String key : catalog.getFieldData().keySet()) {
                    FieldData fd = catalog.getFieldData().get(key);
                    fieldMap.put(key, line.substring(fd.getStart(), fd.getEnd() + 1));
                }

                fieldMap = removeNulls(fieldMap);

                writer.write(getJsonLine(fieldMap));
                //line = reader.readLine();
            }

            writer.close();

            reader.close();
            gzis.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> removeNulls(Map<String, String> map) {

        Map<String, String> result = new LinkedHashMap<String, String>();

        for (String key: map.keySet()) {
            if (map.get(key) != null) {
                result.put(key, map.get(key));
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
        String pattern = "^\\s*[0-9]+$";
        return value.matches(pattern);
    }

    private boolean isDouble(String value) {
        String pattern = "^\\s*[0-9]+\\.[0-9]*$";
        return value.matches(pattern);
    }

    private boolean isNumber(String value) {
        return (isInteger(value) || isDouble(value));
    }
}
