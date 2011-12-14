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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class MenuPanel extends JPanel {

    JMenuBar menuBar;
    JMenu fileMenu;

    public MenuPanel() {
        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");

        // create File menu MenuItems
        JMenuItem exitItem = new JMenuItem("Exit");

        // configure menuitem action listeners
        exitItem.addActionListener(new FileExitActionListener());

        // added menu items to menu
        fileMenu.add(exitItem);

        // add menu to menubar
        menuBar.add(fileMenu);


        menuBar.setPreferredSize(new Dimension(600, 25));
        add(menuBar);
    }

    private class FileExitActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
