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
package org.jason.tdat2json;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DefaultHandler extension to handle parsing of the Catalog Configuration XML files
 *
 * @author Jason Ferguson
 * @since 0.2
 */
public class CatalogHandler extends DefaultHandler {

    private Map<String, Object> configMap = new HashMap<String, Object>();
    private String tagValue;

    public Map<String, Object> getConfigMap() {
        return configMap;
    }

    public CatalogHandler() {
        // set up the fields we are expecting to find with default values (yes, I Know they will return null, but
        // .keyExists() won't work either)
        configMap.put("dropEmpty", true);
        configMap.put("exclusionPatterns", new ArrayList<Pattern>());
        configMap.put("fieldsToDrop", new ArrayList<String>());
        configMap.put("fieldsToCopy", new HashMap<String, String>());
        configMap.put("fieldPrefixes", new HashMap<String, String>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        // deal with copying fields
        if (qName.equalsIgnoreCase("copy")) {
            String fromField = attributes.getValue("fromField");
            String toField = attributes.getValue("toField");
            boolean dropAfterCopy = Boolean.valueOf(attributes.getValue("dropAfterCopy"));
            ((Map<String, String>) configMap.get("fieldsToCopy")).put(fromField, toField);
            if (dropAfterCopy) {
                ((List<String>) configMap.get("fieldsToDrop")).add(fromField);
            }

        }

        // deal with exclusion patterns
        List<Pattern> exclusionPatterns = new ArrayList<Pattern>();
        if (qName.equalsIgnoreCase("exclude")) {
            String pattern = attributes.getValue("pattern");
            String name = attributes.getValue("name");
            if (pattern != null && pattern.length() > 0) {
                exclusionPatterns.add(Pattern.compile(pattern));
            } else if (name != null && name.length() > 0) {
                ((ArrayList<String>) configMap.get("fieldsToDrop")).add(name);
            }

        }
        ((List<Pattern>) configMap.get("exclusionPatterns")).addAll(exclusionPatterns);

        // deal with added field prefixes
        if (qName.equals("prefix")) {
            String field = attributes.getValue("field");
            String text = attributes.getValue("text");
            if (field != null && field.length() > 0 && text != null && text.length() > 0) {
                ((Map<String, String>) configMap.get("fieldPrefixes")).put(field, text);
            }
        }

    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tagValue = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dropEmptyFields")) {
            if (!tagValue.isEmpty()) {
                configMap.put("dropEmpty",Boolean.valueOf(tagValue));
            }
        }
    }
}
