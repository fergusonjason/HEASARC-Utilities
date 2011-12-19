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

import com.google.inject.Inject;
import org.jason.heasarcutils.catalogparser.ui.components.popupMenu.CatalogPopupMenu;

import javax.swing.*;
import java.awt.*;

/**
 * @since 0.2
 * @author Jason Ferguson
 */
public class ApplicationPanel extends JPanel {

    private EditorPanel editorPanel;
    private MenuPanel menuPanel;
    private StatusBarPanel statusBarPanel;
    private TreePanel treePanel;

    private CatalogPopupMenu popupMenu;

    public ApplicationPanel() {
        super(new BorderLayout());

        init();
    }

    @Inject
    public ApplicationPanel(EditorPanel editorPanel, MenuPanel menuPanel, TreePanel treePanel, StatusBarPanel statusBarPanel) {
        super(new BorderLayout());

        this.editorPanel = editorPanel;
        this.menuPanel = menuPanel;
        this.treePanel = treePanel;
        this.statusBarPanel = statusBarPanel;

        init();
    }

    private void init() {
        popupMenu = new CatalogPopupMenu();

        add(menuPanel, BorderLayout.NORTH);
        add(treePanel, BorderLayout.WEST);
        add(editorPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 400));

    }
}
