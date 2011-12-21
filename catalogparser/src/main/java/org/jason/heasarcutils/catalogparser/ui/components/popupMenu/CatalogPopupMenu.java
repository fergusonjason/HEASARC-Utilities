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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.jason.heasarcutils.catalogparser.misc.ConfigMap;
import org.jason.heasarcutils.catalogparser.ui.event.ProcessCatalogEvent;
import org.jason.heasarcutils.catalogparser.ui.event.ShowContextPopupEvent;
import org.jason.heasarcutils.catalogparser.util.Catalog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;

/**
 * Popup menu for use with the JTree
 *
 * @author Jason Ferguson
 * @since 0.2
 */
@SuppressWarnings({"FieldCanBeLocal","unused"})
public class CatalogPopupMenu extends JPopupMenu {

    private EventBus eventBus;
    private ConfigMap config;

    /**
     * Empty constructor, do NOT directly instantiate this. Guice will be mad.
     */
    public CatalogPopupMenu() {}

    /**
     * Guice'd constructor, w/ injected fields
     *
     * @param config     ConfigMap holding the catalog configurations
     * @param eventBus   Guava EventBus singleton
     */
    @Inject
    public CatalogPopupMenu(ConfigMap config, EventBus eventBus) {
        this.config = config;
        this.eventBus = eventBus;

        init();

    }

    private void init() {

        // nothing here since I'm dynamically generating this in the event handler method

    }

    /**
     * Event handler method to manage actions when a ShowContextPopupEvent is received (which is fired
     * when a right click occurs in the tree)
     *
     * @param e     ShowContextPopupEvent
     */
    @Subscribe
    public void handlePopupEvent(ShowContextPopupEvent e) {

        // generate the popup menu on the fly based one what we've got selected
        removeAll();

        // Get the highlighted node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((JTree) e.getComponent()).getLastSelectedPathComponent();

        // If no node was selected during the right click, do nothing
        if (node == null) {
            return;
        }

        // get the text of the object
        final String text = (String) node.getUserObject();

        // Create menu item and set its listener
        JMenuItem importItem = new JMenuItem("Import");
        importItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get the catalog
                Catalog catalog = config.get(text);
                // fire the event to process the catalog
                eventBus.post(new ProcessCatalogEvent(catalog));
            }
        });

        // add the item to the popup menu
        add(importItem);

        // show the popup menu at the designated location
        this.show(e.getComponent(), e.getX(), e.getY());
    }
}
