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
package org.jason.heasarcutils.catalogparser;

import org.jason.heasarcutils.catalogparser.util.ConfigParser;

import java.util.Map;

/**
 * GUI-based application to import data from the HEASARC and Vizier archives
 * <p/>
 * Configuration is currently based on an XML file, unless I can figure out how to embed a configuration
 * database that can be updated by the user.
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class CatalogParser {

    private static void createAndShowGUI(Map<String, Object> config) {
        CatalogParserUserInterface ui = new CatalogParserUserInterface();
        ui.createAndShowGUI(config);
    }

    public static void main(String[] args) {
        CatalogParser app = new CatalogParser();
        ConfigParser configParser = new ConfigParser("config.xml");
        Map<String, Object> config = configParser.getConfig();

        createAndShowGUI(config);
    }
}
