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

import com.google.common.eventbus.EventBus;
import org.jason.heasarcutils.catalogparser.ui.components.popupMenu.CatalogPopupMenu;
import org.jason.heasarcutils.catalogparser.util.Catalog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class TreePanel extends JPanel {

    private JScrollPane scrollPane;
    private JTree tree;
    protected CatalogPopupMenu popupMenu;
    private Map<String, Catalog> config;

    private EventBus eventBus;

    // constructor takes a backref to the enclosing component so I can get its size, etc
    public TreePanel(EventBus eventBus, Map<String, Catalog> config) {
        super();
        this.eventBus = eventBus;
        this.config = config;
        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {

        popupMenu = new CatalogPopupMenu(eventBus, getTree(), config);
        JMenuItem exportMenuItem = new JMenuItem("Export To JSON");
        popupMenu.add(exportMenuItem);

        DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Catalogs");

        // create the tree nodes
        for (String key: config.keySet()) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(key);
            topNode.add(treeNode);
        }
        tree = new JTree(topNode);
        // set listeners on the tree
        tree.addMouseListener(new TreeContextPopupMenuListener());
        tree.setPreferredSize(new Dimension(200, 600));

        scrollPane = new JScrollPane(tree);

        add(scrollPane);
    }


    public JTree getTree() {
        return tree;
    }

    public void setPopupMenu(CatalogPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    class TreeContextPopupMenuListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {

            if (popupMenu != null) {
                if (e.isPopupTrigger()) {
                    doPopup(e);
                }
            }
        }

        public void mouseReleased(MouseEvent e) {

            if (popupMenu != null) {
                if (e.isPopupTrigger()) {
                    doPopup(e);
                }
            }
        }

        public void doPopup(MouseEvent e) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
