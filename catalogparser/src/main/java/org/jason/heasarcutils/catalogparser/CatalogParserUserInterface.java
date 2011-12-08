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

import org.jason.heasarcutils.catalogparser.util.Catalog;
import org.jason.heasarcutils.catalogparser.util.JsonExporter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Set;

/**
 * User Interface for CatalogParser app
 *
 * @author Jason Ferguson
 * @since 0.1
 */
public class CatalogParserUserInterface {

    private static JTree createTreeControl(final Map<String, Object> config) {

        DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Catalogs");


        Set<String> catalogNameSet = config.keySet();
        for (String catalogName : catalogNameSet) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(catalogName);
            topNode.add(node);
        }
        final JTree tree = new JTree(topNode);
        tree.setSize(200, 600);
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem exportToJsonItem = new JMenuItem("Export to JSON");
        exportToJsonItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TreePath[] treePaths = tree.getSelectionPaths();
                if (treePaths != null && treePaths.length > 0) {
                    TreePath path = treePaths[0];
                    String catalog = (String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                    JsonExporter.exportToJSON((Catalog) config.get(catalog));
                }

            }
        });
        popupMenu.add(exportToJsonItem);

        tree.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        return tree;
    }

    private static JMenuBar createMenuBarControl() {

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        return menuBar;
    }

    private static void addComponentsToPane(Container pane, Map<String, Object> config) {
        pane.add(createMenuBarControl(), BorderLayout.PAGE_START);
        pane.add(createTreeControl(config), BorderLayout.BEFORE_LINE_BEGINS);

    }

    public void createAndShowGUI(Map<String, Object> config) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Astronomical Catalog Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addComponentsToPane(frame.getContentPane(), config);
        //frame.pack();
        frame.setSize(600, 400);
        frame.setVisible(true);

    }
}
