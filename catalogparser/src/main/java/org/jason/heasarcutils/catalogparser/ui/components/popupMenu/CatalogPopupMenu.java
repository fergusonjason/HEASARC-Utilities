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
package org.jason.heasarcutils.catalogparser.ui.components.popupMenu;

import org.jason.heasarcutils.catalogparser.util.Catalog;
import org.jason.heasarcutils.catalogparser.util.JsonExporter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class CatalogPopupMenu extends JPopupMenu {

    private JTree parent;
    private Map<String, Catalog> config;

    // provide backreference to the JTree this will be attached to
    public CatalogPopupMenu(JTree parent, Map<String, Catalog> config) {
        this.parent = parent;

        init();
    }

    private void init() {

        // create menu items
        JMenuItem exportToJsonMenuItem = new JMenuItem("Export to JSON");

        // add listeners to menu items
        exportToJsonMenuItem.addActionListener(new ExportToJsonListener(config));
    }

    public class ExportToJsonListener implements ActionListener {

        private Map<String, Catalog> config;

        public ExportToJsonListener(Map<String, Catalog> config) {
            this.config = config;
        }

        public void actionPerformed(ActionEvent e) {

//            JScrollPane scrollPane = (JScrollPane) treePanel.getComponent(0);
//            JTree tree = (JTree) ((JViewport) scrollPane.getComponent(0)).getView();
//
//            JStatusBar statusBar = (JStatusBar) statusBarPanel.getComponent(0);

            TreePath[] treePaths = parent.getSelectionPaths();
            if (treePaths != null && treePaths.length > 0) {
                TreePath path = treePaths[0];
                String catalog = (String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                JsonExporter jsonExporter = new JsonExporter().setCatalog(config.get(catalog));
                //statusBar.setTextWhenEmpty("");
                //statusBar.setText("Exporting...");
                jsonExporter.exportToJSON();
                //statusBar.setText("Export Complete.");
            }
        }
    }
}
