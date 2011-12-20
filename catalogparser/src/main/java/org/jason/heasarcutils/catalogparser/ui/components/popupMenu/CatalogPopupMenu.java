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
import org.jason.heasarcutils.catalogparser.ui.event.statusBar.SetStatusBarTextEvent;
import org.jason.heasarcutils.catalogparser.util.Catalog;

import javax.swing.*;
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

        for (String catalogName : config.keySet()) {
            // create menu items
            final Catalog catalog = config.get(catalogName);
            final String name = config.get(catalogName).getName();

            JMenuItem exportToJsonMenuItem = new JMenuItem("Export to JSON: " + name);

            // add listeners to menu items
            exportToJsonMenuItem.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // fire a ExportJSONAction
                    eventBus.post(new SetStatusBarTextEvent("Exporting catalog"));
                    //eventBus.post(new ExportJsonEvent(config.get(catalog)));

                }
            });

            add(exportToJsonMenuItem);
        }

    }

    @Subscribe
    public void handlePopupEvent(ShowContextPopupEvent e) {
        this.show(e.getComponent(), e.getX(), e.getY());
    }
}
