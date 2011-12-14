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

import javax.swing.*;
import java.awt.*;

/**
 * Main Frame component for the application
 *
 * @since 0.2
 * @author Jason Ferguson
 */
public class ApplicationFrame extends JFrame {

    private JPanel applicationPanel = new JPanel(new BorderLayout());

    private EditorPanel editorPanel;
    private MenuPanel menuPanel;
    private StatusBarPanel statusBarPanel;
    private TreePanel treePanel;


    public ApplicationFrame() throws HeadlessException {
        //init();
    }

    /**
     * Init components specific to this component
     */
    public void init() {

        if (System.getProperty("os.name").contains("indows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editorPanel = new EditorPanel();
        menuPanel = new MenuPanel();
        statusBarPanel = new StatusBarPanel();
        treePanel = new TreePanel();

        setTitle("Astronomical Catalog Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setPreferredSize(new Dimension(600, 400));

        applicationPanel.add(menuPanel, BorderLayout.NORTH);
        applicationPanel.add(treePanel, BorderLayout.WEST);
        applicationPanel.add(editorPanel, BorderLayout.CENTER);
        applicationPanel.add(statusBarPanel, BorderLayout.SOUTH);

        setSize(600, 400);

        add(applicationPanel);
        setVisible(true);
    }
}
