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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jason.heasarcutils.catalogparser.misc.ConfigMap;
import org.jason.heasarcutils.catalogparser.ui.components.popupMenu.CatalogPopupMenu;
import org.jason.heasarcutils.catalogparser.ui.event.ShowContextPopupEvent;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JPanel containing the tree control
 *
 * @author Jason Ferguson
 * @since 0.2
 */
@SuppressWarnings({"unused"})
@Singleton
public class TreePanel extends JPanel {

    private JScrollPane scrollPane;
    private JTree tree;
    private ConfigMap config;
    private EventBus eventBus;

    public TreePanel() {}

    @Inject
    public TreePanel(EventBus eventBus, ConfigMap config) {
        super();
        this.eventBus = eventBus;
        this.config = config;
        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {

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

    public class TreeContextPopupMenuListener extends MouseAdapter {

        private CatalogPopupMenu popupMenu;


        @Override
        public void mousePressed(MouseEvent e) {

//            doPopup(e);

        }

        @Override
        public void mouseReleased(MouseEvent e) {

            doPopup(e);

        }

        public void doPopup(MouseEvent e) {
            ShowContextPopupEvent event = new ShowContextPopupEvent(e.getComponent(), e.getX(), e.getY());
            eventBus.post(event);
        }
    }
}
