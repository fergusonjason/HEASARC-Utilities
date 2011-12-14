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

import org.jason.heasarcutils.catalogparser.ui.components.popupMenu.CatalogPopupMenu;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class TreePanel extends JPanel {

    private JComponent parent;
    private JScrollPane scrollPane;
    private JTree tree;
    private CatalogPopupMenu popupMenu;

    // constructor takes a backref to the enclosing component so I can get its size, etc
    public TreePanel(JComponent parent) {
        super();
        this.parent = parent;
        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {

        DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Catalogs");
        tree = new JTree(topNode);

        // create the tree nodes

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

    private class TreeContextPopupMenuListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {

        }

        public void mousePressed(MouseEvent e) {
            if (popupMenu != null) {
                if (e.isPopupTrigger()) {
                    popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (popupMenu != null) {
                if (e.isPopupTrigger()) {
                    popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }
        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }
    }
}
