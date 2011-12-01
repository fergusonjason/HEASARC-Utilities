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
package org.jason.heasarcutils.vizier2json;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Utility to convert Vizier catalog data to JSON format for use with MongoDB/Other JSON utilities
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class Vizier2Json {

    private static String config = "classes\\vizier.xml";
    private static Document dom;
    private static Map<String, Catalog> catalogMap = new HashMap<String, Catalog>();

    public Vizier2Json() {


    }

    private static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    public static Map<String, Catalog> parseConfig() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(new File(config));
            Element e = dom.getDocumentElement();
            // get the <catalog> tags
            NodeList nl = e.getElementsByTagName("catalog");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Catalog catalog = new Catalog();
                    Element element = (Element) nl.item(i);

                    // set the easy stuff
                    catalog.setName(getTextValue(element, "name"));
                    catalog.setUrl(getTextValue(element, "url"));

                    // get the <fields> tag for this catalog
                    Element fields = (Element) e.getElementsByTagName("fields").item(0);

                    // get a nodelist of all of the <field> tags within the <fields> parent tag
                    NodeList individualFields = fields.getElementsByTagName("field");
                    for (int j = 0; j < individualFields.getLength(); j++) {
                        // get the <field> tag
                        Element individualField = (Element) individualFields.item(j);
                        // process the name, start, and end attributes
                        String ifName = individualField.getAttribute("name");
                        Integer ifStart = new Integer(individualField.getAttribute("start"));
                        Integer ifEnd = new Integer(individualField.getAttribute("end"));
                        // add a new entry to the Catalog's field map
                        catalog.getFieldData().put(ifName, new FieldData(ifStart, ifEnd));
                    }

                    // get the prefixes
                    Element prefixes = (Element) e.getElementsByTagName("prefixes").item(0);

                    NodeList individualPrefixes = prefixes.getElementsByTagName("prefix");
                    Map<String, String> prefixMap = new HashMap<String, String>();
                    for (int j = 0; j < individualPrefixes.getLength(); j++) {
                        // get the <prefix> tag
                        Element individualPrefix = (Element) individualPrefixes.item(j);
                        //process the name and prefix attributes
                        String ipName = individualPrefix.getAttribute("name");
                        String ipPrefix = individualPrefix.getAttribute("text");
                        prefixMap.put(ipName, ipPrefix);
                        catalog.setPrefixes(prefixMap);
                    }

                    catalogMap.put(catalog.getName(), catalog);
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return catalogMap;

    }

    public void parseCatalog(String catalogName) {

        Catalog catalog = catalogMap.get(catalogName);
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog Not Found in Configuration: " + catalogName);
        }
        String fileurl = catalog.getUrl();
        Map<String, FieldData> fieldMap = catalog.getFieldData();
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        try {
            URL url = new URL(fileurl);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            GZIPInputStream gzis = new GZIPInputStream(is);
            BufferedReader isReader = new BufferedReader(new InputStreamReader(gzis));
            BufferedWriter writer = new BufferedWriter(new FileWriter(catalogName + ".json"));
            String line;
            while ((line = isReader.readLine()) != null) {
                int lineLength = line.length();
                // dump the raw values into the result map based on the configuration data
                for (String fieldKey : fieldMap.keySet()) {
                    FieldData fieldData = fieldMap.get(fieldKey);
                    int start = fieldData.getStart() - 1;
                    int end = fieldData.getEnd();
                    // the record may end before the definition in the xml
                    if (lineLength >= start) {
                        if (lineLength <= end) {
                            String value = line.substring(start);
                            resultMap.put(fieldKey, value.trim());
                        } else {
                            String value = line.substring(start, end);
                            resultMap.put(fieldKey, value.trim());
                        }
                    }
                }
                // process the values in the result map before we put them into the json output
                // deal with the prefixes
                Map<String, String> prefixMap = catalog.getPrefixes();
                for (String key: prefixMap.keySet()) {
                    String value = resultMap.get(key);
                    if (value != null) {
                        value = prefixMap.get(key) + value;
                        resultMap.put(key, value);
                    }
                }
                String jsonLine = convertToJson(resultMap);
                writer.write(jsonLine);
                writer.write("\r\n");
            }

            writer.close();
            isReader.close();
            gzis.close();
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (StringIndexOutOfBoundsException e) {
            // I want this swallowed, even if it is a RuntimeException
            e.printStackTrace();
        }

    }

    public static boolean isNumeric(String value) {
        String pattern = "^[-]*[0-9]+\\.*[0-9]*$";
        return (value.matches(pattern));
    }

    public String convertToJson(Map<String, String> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (String key: map.keySet()) {
            sb.append("\"").append(key).append("\":");
            if (isNumeric(map.get(key))) {
                BigDecimal number = new BigDecimal(map.get(key));
                sb.append(number).append(",");
            } else {
                sb.append("\"").append(map.get(key)).append("\",");
            }
        }
        // strip the trailing comma
        sb = new StringBuffer(sb.substring(0,sb.length() - 1));
        sb.append("}\r\n");
        return sb.toString();
    }
    public static void main(String[] args) {
        Vizier2Json v2j = new Vizier2Json();
        Map<String, Catalog> catalogMap = parseConfig();
        v2j.parseCatalog(args[0]);

    }
}
