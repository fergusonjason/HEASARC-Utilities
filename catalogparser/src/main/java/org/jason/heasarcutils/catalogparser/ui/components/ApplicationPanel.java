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
import org.jason.heasarcutils.catalogparser.ui.components.popupMenu.CatalogPopupMenu;

import javax.swing.*;
import java.awt.*;

/**
 * @since 0.2
 * @author Jason Ferguson
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Singleton
public class ApplicationPanel extends JPanel {

    private EventBus eventBus;

    private EditorPanel editorPanel;
    private MenuPanel menuPanel;
    private StatusBarPanel statusBarPanel;
    private TreePanel treePanel;

    private CatalogPopupMenu popupMenu;

    /**
     * Empty constructor so Java doesn't bitch when I initialize it. Guava will take over and use the other
     * constructor to initialize.
     */
    public ApplicationPanel() {
        super(new BorderLayout());

        init();
    }

    /**
     * Guice'd version of the constructor, annotated w/ @Inject in order to inject the various components of the
     * Swing UI.
     *
     * @param eventBus          Guava EventBus (initialized as a Singleton in the Module class)
     * @param editorPanel       JPanel containing the editor window
     * @param menuPanel         JPanel containing the application menubar
     * @param treePanel         JPanel containing the application tree control
     * @param statusBarPanel    JPanel containing the application status bar
     * @param popupMenu         JPopupMenu containing the context popup menu used with the tree control
     */
    @Inject
    public ApplicationPanel(EventBus eventBus,
                            EditorPanel editorPanel,
                            MenuPanel menuPanel,
                            TreePanel treePanel,
                            StatusBarPanel statusBarPanel,
                            CatalogPopupMenu popupMenu) {
        super(new BorderLayout());

        this.eventBus = eventBus;

        this.editorPanel = editorPanel;
        this.menuPanel = menuPanel;
        this.treePanel = treePanel;
        this.statusBarPanel = statusBarPanel;
        this.popupMenu = popupMenu;

        eventBus.register(this.editorPanel);
        eventBus.register(this.popupMenu);

        init();
    }

    /**
     * Method to initialize the component
     */
    private void init() {

        add(menuPanel, BorderLayout.NORTH);
        add(treePanel, BorderLayout.WEST);
        add(editorPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 400));

    }
}
