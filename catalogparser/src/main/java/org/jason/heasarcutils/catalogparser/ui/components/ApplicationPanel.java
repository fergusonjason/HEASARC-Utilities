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
import org.jason.heasarcutils.catalogparser.util.Catalog;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * @since 0.2
 * @author Jason Ferguson
 */
public class ApplicationPanel extends JPanel {

    private EventBus eventBus;

    private JFrame parent;

    private EditorPanel editorPanel;
    private MenuPanel menuPanel;
    private StatusBarPanel statusBarPanel;
    private TreePanel treePanel;

    //private CatalogPopupMenu popupMenu;

    private Map<String, Catalog> config;

    // constructor takes reference to parent jframe
    public ApplicationPanel(EventBus eventBus, JFrame parent, Map<String, Catalog> config) {
        super(new BorderLayout());
        this.eventBus = eventBus;
        this.parent = parent;
        this.config = config;

        init();
    }

    private void init() {
        editorPanel = new EditorPanel(eventBus);
        menuPanel = new MenuPanel();
        statusBarPanel = new StatusBarPanel();
        treePanel = new TreePanel(eventBus, config);

        add(menuPanel, BorderLayout.NORTH);
        add(treePanel, BorderLayout.WEST);
        add(editorPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 400));

    }
}
