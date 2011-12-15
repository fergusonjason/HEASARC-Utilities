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
package org.jason.heasarcutils.catalogparser.ui.components;

import org.jason.heasarcutils.catalogparser.util.Catalog;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Main Frame component for the application
 *
 * @author Jason Ferguson
 * @since 0.2
 */
public class ApplicationFrame extends JFrame {

    //private ApplicationPanel applicationPanel = new ApplicationPanel(this);
    ApplicationPanel applicationPanel;

    private Map<String, Catalog> config;

    public ApplicationFrame(Map<String, Catalog> config) throws HeadlessException {
        this.config = config;
        //init();
    }

    /**
     * Init components specific to this component
     */
    public void init() {
        if (System.getProperty("os.name").contains("indows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                SwingUtilities.updateComponentTreeUI(ApplicationFrame.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        applicationPanel = new ApplicationPanel(this, config);

        setTitle("Astronomical Catalog Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(600,400);
        add(applicationPanel);
        setVisible(true);
    }
}
