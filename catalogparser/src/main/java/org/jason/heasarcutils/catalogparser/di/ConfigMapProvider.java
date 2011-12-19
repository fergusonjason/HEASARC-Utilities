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
package org.jason.heasarcutils.catalogparser.di;

import com.google.inject.Provider;
import org.jason.heasarcutils.catalogparser.misc.ConfigMap;
import org.jason.heasarcutils.catalogparser.util.ConfigParser;

/**
 * @since 0.2
 * @author Jason Ferguson
 */
public class ConfigMapProvider implements Provider<ConfigMap> {

    private ConfigMap config = new ConfigMap();

    public ConfigMapProvider() {
        this.config.putAll(new ConfigParser("config.xml").getConfig());
    }

    @Override
    public ConfigMap get() {
        return config;
    }
}
