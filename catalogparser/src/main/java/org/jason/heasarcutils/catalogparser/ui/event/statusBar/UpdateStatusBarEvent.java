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
package org.jason.heasarcutils.catalogparser.ui.event.statusBar;

import org.jason.heasarcutils.catalogparser.ui.event.ApplicationEvent;

/**
 * @since 0.2.1
 * @author Jason Ferguson
 */
public class UpdateStatusBarEvent implements ApplicationEvent {

    private String message;
    private int min;
    private int max;
    private int value;

    public UpdateStatusBarEvent(int max, String message, int min, int value) {
        this.max = max;
        this.message = message;
        this.min = min;
        this.value = value;
    }

    public int getMax() {
        return max;
    }

    public String getMessage() {
        return message;
    }

    public int getMin() {
        return min;
    }

    public int getValue() {
        return value;
    }
}
