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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author Jason Ferguson
 * @since 0.2
 */
public class TreePanel extends JPanel {

    JScrollPane scrollPane;
    JTree tree;

    public TreePanel() {
        init();
    }

    /**
     * Init components specific to this component
     */
    private void init() {
        scrollPane = new JScrollPane();

        DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Catalogs");
        tree = new JTree(topNode);

        tree.setPreferredSize(new Dimension(200,600));
        scrollPane.add(tree);
        add(scrollPane);
    }
}
