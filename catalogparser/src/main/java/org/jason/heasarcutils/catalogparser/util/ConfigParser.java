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

import org.jason.heasarcutils.catalogparser.misc.ConfigurationParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Parsers for configuration XML. Uses DOM for XML processing, not SAX
 *
 * This class demonstrates the implementation of the Strategy pattern (though the Visitor
 * pattern probably would have worked just as well).
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class ConfigParser {

    private String configFile;

    public ConfigParser(String configFile) {
        this.configFile = "classes" + System.getProperty("file.separator") + configFile;
    }

    /**
     * Public entry to config parser class
     *
     * @return  a Map containing catalog names mapped to the corresponding Catalog objects
     */
    public Map<String, Catalog> getCatalogs() {
        Map<String, Catalog> catalogMap = new HashMap<String, Catalog>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(configFile)); // TODO: This should probably use getResourceAsStream
            NodeList catalogNodes = document.getElementsByTagName("catalog");
            for (int i = 0; i < catalogNodes.getLength(); i++) {
                Element catalogNode = (Element) catalogNodes.item(i);
                Catalog catalog = getCatalog(catalogNode);
                catalogMap.put(catalog.getName(), catalog);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return catalogMap;
    }

    /**
     * Create a single Catalog object from an Element representing a <catalog> tag
     *
     * @param catalogNode   Element representing a single <catalog> node
     * @return  a Catalog object populated from the Element contents
     */
    private Catalog getCatalog(Element catalogNode) {

        Catalog catalog = new Catalog();

        // get basic info from the attributes of the <catalog> tag
        catalog.setName(catalogNode.getAttribute("name"));
        catalog.setType(catalogNode.getAttribute("type"));

        // get the information from the sub-elements of the <catalog>
        catalog.setUrl(getTextValue(catalogNode, "url"));
        catalog.setTitle(getTextValue(catalogNode, "title"));
        catalog.setDescription(getTextValue(catalogNode, "description"));
        catalog.setEpoch(getTextValue(catalogNode, "epoch"));
        catalog.setTotalRecords(Integer.valueOf(getTextValue(catalogNode, "totalRecords")));

        // create the Strategy context to determine how to process the file based on its type
        Context context;
        if (catalog.getType().equalsIgnoreCase("TDAT")) {
            context = new Context(new TdatStrategy(catalog, catalogNode));
        } else {
            context = new Context(new DatStrategy(catalog, catalogNode));
        }

        // process the fields
        context.processFields();

        return catalog;
    }

    /**
     * Create a Buffered Reader based on a GZipInputStream, since there isn't any sort of
     * GZipReader class)
     *
     * @param fileUrl String representing the URL of the remote file
     * @return a BufferedReader from the URL
     * @throws IOException thrown when something goes wrong creating a reader
     */
    private BufferedReader createGzipReader(String fileUrl) throws IOException {

        GZIPInputStream gzis = new GZIPInputStream(createInputStream(fileUrl));
        InputStreamReader isr = new InputStreamReader(gzis, "UTF-8");

        return new BufferedReader(isr);
    }

    /**
     * Create an InputStream from a String representing a remote URL
     *
     * @param urlLocation String representing the URL of the remote file
     * @return an InputStream from the remote file
     * @throws IOException thrown when something goes wrong creating a reader
     */
    private InputStream createInputStream(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return url.openStream();
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


    private boolean isInteger(String value) {
        String pattern = "^[0-9]+$";
        return value.matches(pattern);
    }

    /**
     * Interface for Strategy objects used to decide how to process objects being read from an external file
     */
    private interface Strategy {

        void processFields();
    }

    /**
     * Holder object for the strategy
     */
    private class Context {

        Strategy strategy;

        public Context(Strategy strategy) {
            this.strategy = strategy;
        }

        public void processFields() {
            this.strategy.processFields();
        }

    }

    /**
     * Parent class for Strategy implementations. Defines getFieldData2() since (so far) all implementations use it
     */
    private abstract class AbstractStrategy implements Strategy {

        protected Element catalogNode;
        protected Catalog thisCatalog;

        protected AbstractStrategy(Catalog thisCatalog, Element catalogNode) {
            this.thisCatalog = thisCatalog;
            this.catalogNode = catalogNode;
        }

        public abstract void processFields();

        /**
         * Method to get field data as a Set. The sorted set has to be defined using a comparator
         * that sorted on the FieldData object's start field in order to keep them in proper order when the
         * file is being read later.
         *
         * @param catalogNode Element representing contents of s single <catalog>
         * @return a Set of FieldData objects, each representing a <field> element
         */
        protected Set<FieldData> getFieldData2(Element catalogNode) {
            Set<FieldData> result = new HashSet<FieldData>();

            Element fieldsNode = (Element) catalogNode.getElementsByTagName("fields").item(0);
            NodeList fieldNodeList = fieldsNode.getElementsByTagName("field");
            for (int i = 0; i < fieldNodeList.getLength(); i++) {
                Element fieldNode = (Element) fieldNodeList.item(i);
                String name = fieldNode.getAttribute("name");
                if (name.isEmpty()) {
                    throw new ConfigurationParseException("Attribute 'name' of tag 'field' cannot be empty.");
                }
                String rename = fieldNode.getAttribute("renameTo");
                String prefix = fieldNode.getAttribute("prefix");
                String keepAfterCopy = fieldNode.getAttribute("keepAfterCopy");
                String start = fieldNode.getAttribute("start");
                String end = fieldNode.getAttribute("end");

                FieldData fd = new FieldData();
                fd.setName(name);
                if (!rename.isEmpty()) {
                    fd.setRenameTo(rename);
                }
                if (!prefix.isEmpty()) {
                    fd.setPrefix(prefix);
                }
                if (!keepAfterCopy.isEmpty()) {
                    boolean kac = Boolean.valueOf(keepAfterCopy);
                    fd.setKeepAfterCopy(kac);
                }
                if (isInteger(start)) {
                    fd.setStart(Integer.parseInt(start));
                }
                if (isInteger(end)) {
                    fd.setEnd(Integer.parseInt(end));
                }
                fd.setIncluded(true);
                result.add(fd);
            }

            return result;
        }
    }

    /**
     * Strategy for handling Dat objects
     */
    private class DatStrategy extends AbstractStrategy {

        private DatStrategy(Catalog thisCatalog, Element catalogNode) {
            super(thisCatalog, catalogNode);
        }

        /**
         * DAT-specific implementation of processFields(). Not as complex as the TDAT implementation,
         * but there aren't any header files to make this easier
         */
        @Override
        public void processFields() {

            Set<FieldData> fieldDataSet = getFieldData2(catalogNode);
            for (FieldData fd : fieldDataSet) {
                thisCatalog.getFieldData().put(fd.getName(), fd);
            }
        }
    }

    /**
     * Strategy for handling TDAT objects. Contains getFieldNamesFromTdatHeader() since it's only needed to input
     * TDAT files
     */
    private class TdatStrategy extends AbstractStrategy {

        private TdatStrategy(Catalog thisCatalog, Element catalogNode) {
            super(thisCatalog, catalogNode);
        }

        /**
         * TDAT specific  implementation of processFields
         */
        @Override
        public void processFields() {

            thisCatalog.setHeaderUrl(getTextValue(catalogNode, "headerUrl"));
            Set<FieldData> fieldDataSet = getFieldData2(catalogNode);
            for (FieldData fd : fieldDataSet) {
                thisCatalog.getFieldData().put(fd.getName(), fd);
            }
            String[] fields = getFieldNamesFromTdatHeader(thisCatalog.getHeaderUrl());

            for (String field : fields) {
                thisCatalog.getFieldData().put(field, new FieldData(false));
            }

            for (FieldData fd : thisCatalog.getFieldDataSet()) {
                thisCatalog.getFieldData().put(fd.getName(), fd);
            }

        }

        /**
         * Since TDAT files have an associated header, we can get the field names from the line[n]
         * within that file
         *
         * @param headerFile String representing location of the header file
         * @return String array containing the fields defined by the tdat header
         */
        private String[] getFieldNamesFromTdatHeader(String headerFile) {

            BufferedReader reader = null;
            String linePattern = "line\\[1\\] = (.*)";
            String[] fields = null;
            try {
                reader = createGzipReader(headerFile);
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.matches(linePattern)) {
                        Pattern pattern = Pattern.compile(linePattern);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            fields = matcher.group(1).split("\\s");
                            break;
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeQuietly(reader);
            }

            return fields;
        }
    }
}
