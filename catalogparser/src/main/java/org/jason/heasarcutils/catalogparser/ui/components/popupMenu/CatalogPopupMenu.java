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
import org.jason.heasarcutils.catalogparser.ui.event.ShowContextPopupEvent;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;

/**
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

    }

    @Subscribe
    public void handlePopupEvent(ShowContextPopupEvent e) {

        // generate the popup menu on the fly based one what we've got selected
        removeAll();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((JTree) e.getComponent()).getLastSelectedPathComponent();
        final String text = (String) node.getUserObject();

        JMenuItem importItem = new JMenuItem("Import");
        importItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Importing catalog " + text);
            }
        });

        JMenuItem exportItem = new JMenuItem("Export");
        exportItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Exporting catalog " + text);
            }
        });

        add(importItem);
        add(exportItem);

        this.show(e.getComponent(), e.getX(), e.getY());
    }
}
