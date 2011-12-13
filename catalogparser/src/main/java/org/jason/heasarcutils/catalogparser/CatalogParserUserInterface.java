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

import org.jason.heasarcutils.catalogparser.misc.JStatusBar;
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
public class CatalogParserUserInterface extends JFrame {

    protected JPanel mainApplicationPanel = new JPanel(new BorderLayout());
    protected JStatusBar statusBar = new JStatusBar();
    protected JPanel treePanel = new JPanel();
    protected JPanel statusBarPanel = new JPanel();

    private JScrollPane createTreePane(Map<String, Catalog> config) {

        DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Catalogs");

        // Create a  node for each catalog in the config file
        Set<String> catalogNameSet = config.keySet();
        for (String catalogName : catalogNameSet) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(catalogName);
            topNode.add(node);
        }

        final JTree tree = new JTree(topNode);
        tree.setSize(200, 600);
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem exportToJsonItem = new JMenuItem("Export to JSON");
        exportToJsonItem.addActionListener(new ExportToJsonListener(config));
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

        JScrollPane scrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JMenuBar createMenuBarControl() {

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

    private JScrollPane createEditorPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(true);
        editorPane.setBorder(BorderFactory.createLoweredBevelBorder());

        JScrollPane scrollPane = new JScrollPane(editorPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    public void createAndShowGUI(Map<String, Catalog> config) {
        // set to windows look and feel if we are running in windows.
        if (System.getProperty("os.name").contains("indows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setTitle("Astronomical Catalog Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        JMenuBar appMenuBar = createMenuBarControl();
        mainApplicationPanel.add(appMenuBar, BorderLayout.NORTH);

        JScrollPane treePane = createTreePane(config);
        treePane.setPreferredSize(new Dimension(100, 600));
        treePanel.add(treePane);
        mainApplicationPanel.add(treePanel, BorderLayout.WEST);

        JScrollPane editorPane = createEditorPane();
        mainApplicationPanel.add(editorPane, BorderLayout.CENTER);

        statusBar = new JStatusBar();
        statusBarPanel.add(statusBar);
        statusBarPanel.setPreferredSize(new Dimension(400, 25));

        mainApplicationPanel.add(statusBarPanel, BorderLayout.SOUTH);

        add(mainApplicationPanel);
        setVisible(true);
        getComponents();
    }

    public JPanel getMainApplicationPanel() {
        return mainApplicationPanel;
    }

    public JStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Actual implementation of an ActionListener for the Export to JSON option. A real implementation
     * gets around the static context issues with an anonymous implementation of ActionListener. Once
     * again, thank the Oracle/Sun dudes for lack of closures in the language.
     */
    public class ExportToJsonListener implements ActionListener {

        private Map<String, Catalog> config;

        public ExportToJsonListener(Map<String, Catalog> config) {
            this.config = config;
        }

        // todo: too much logic for just a listener!
        public void actionPerformed(ActionEvent e) {

            JScrollPane scrollPane = (JScrollPane) treePanel.getComponent(0);
            JTree tree = (JTree) ((JViewport) scrollPane.getComponent(0)).getView();

            JStatusBar statusBar = (JStatusBar) statusBarPanel.getComponent(0);

            TreePath[] treePaths = tree.getSelectionPaths();
            if (treePaths != null && treePaths.length > 0) {
                TreePath path = treePaths[0];
                String catalog = (String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                JsonExporter jsonExporter = new JsonExporter().setCatalog(config.get(catalog));
                statusBar.setTextWhenEmpty("");
                statusBar.setText("Exporting...");
                jsonExporter.exportToJSON();
                statusBar.setText("Export Complete.");
            }
        }
    }
}
