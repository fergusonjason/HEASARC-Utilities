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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jason.heasarcutils.catalogparser.misc.JStatusBar;
import org.jason.heasarcutils.catalogparser.ui.event.statusBar.SetStatusBarTextEvent;

import javax.swing.*;

/**
 * Represents the status bar at the bottom of the UI
 *
 * @since 0.2
 * @author Jason Ferguson
 */
@SuppressWarnings({"unused","FieldCanBeLocal"})
@Singleton
public class StatusBarPanel extends JPanel{

    private EventBus eventBus;

    private JStatusBar statusBar;

    public StatusBarPanel() {
        init();
    }

    @Inject
    public StatusBarPanel(EventBus eventBus) {
        this.eventBus = eventBus;

        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {
        statusBar = new JStatusBar();
        add(statusBar);
    }

    @Subscribe
    public void updateStatusBarText(SetStatusBarTextEvent e) {
        statusBar.setText(e.getText());
    }
}
