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
import com.google.common.eventbus.Subscribe;
import org.jason.heasarcutils.catalogparser.ui.event.PopulateEditorEvent;

import javax.swing.*;

/**
 * Represents the panel that contains the Editor pane.
 *
 * This class handles the following events:
 * - PopulateEditorEvent - fired by the Tree panel when an item in the tree is highlighted. The editor pane is
 *   <em>supposed</em> to load the first 20 or so lines of the associated catalog (in JSON format), or display
 *   a message saying that the catalog needs to be populated
 *
 * @author Jason Ferguson
 * @since 0.2
 */
public class EditorPanel extends JPanel {

    private EventBus eventBus;

    private JScrollPane scrollPane;
    private JEditorPane editorPane;

    public EditorPanel(EventBus eventBus) {
        this.eventBus  = eventBus;
        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {

        editorPane = new JEditorPane();

        editorPane.setEditable(true);
        editorPane.setBorder(BorderFactory.createLoweredBevelBorder());

        scrollPane = new JScrollPane(editorPane);

        add(scrollPane);
    }

    @Subscribe
    public void handlePopulateEditor(PopulateEditorEvent event) {

        editorPane.setText("This catalog has not been imported.");
    }
}
