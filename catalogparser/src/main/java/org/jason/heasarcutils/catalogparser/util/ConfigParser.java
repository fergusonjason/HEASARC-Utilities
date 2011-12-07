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
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parsers for configuration XML. Uses DOM for XML processing, not SAX
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class ConfigParser {

    public Map<String, Object> getConfig(String configFile) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(configFile)); // TODO: This should probably use getResourceAsStream
            NodeList catalogNodes = document.getElementsByTagName("catalog");
            for (int i=0; i< catalogNodes.getLength(); i++) {
                Catalog catalog = new Catalog();
                Element catalogNode = (Element) catalogNodes.item(i);
                String type = catalogNode.getAttribute("type");
                if (type==null || type.isEmpty()) {
                    throw new ConfigurationParseException("Attribute 'type' cannot be null or empty");
                }
                if (!(type.equals("tdat") || type.equals("dat"))) {
                    throw new ConfigurationParseException("Attribute value for 'type' must be 'tdat' or 'dat");
                }
                if (type.equals("tdat")) {
                    // download the header file

                    // parse the header file for the field names
                    // field lengths are variable, need to parse via breaking on pipes
                } else {
                    // field lengths are constant
                }
            }
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }

        return null;
    }

    private Catalog getTdatConfig() {
        // regex pattern to find the field names
        Pattern fieldNameRegexPattern = Pattern.compile("line\\[1\\] = (.*)");
        return null;
    }

    private Catalog getDatConfig() {

        return null;
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

    private String getUrl(Element catalogNode) {
        String url = getTextValue(catalogNode, "url");
        if (url.isEmpty()) {
            throw new ConfigurationParseException("Attribute 'url' cannot be empty.");
        }
        return url;
    }

    private String getHeaderUrl(Element catalogNode) {
        String headerUrl = getTextValue(catalogNode, "headerUrl");
        if (headerUrl.isEmpty()) {
            throw new ConfigurationParseException("Attribute 'headerUrl' cannot be empty.");
        }

        return headerUrl;
    }

    private String getEpoch(Element catalogNode) {
        String epoch = getTextValue(catalogNode, "epoch");
        if (epoch.isEmpty()) {
            throw new ConfigurationParseException("Attribute 'epoch' cannot be empty");
        }
        return epoch;
    }
}
