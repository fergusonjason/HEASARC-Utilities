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

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.jason.heasarcutils.catalogparser.misc.ConfigMap;
import org.jason.heasarcutils.catalogparser.util.ConfigParser;

import javax.inject.Singleton;

/**
 * Guice Module binding for application so I can inject the eventbus
 *
 * @since 0.2
 * @author Jason Ferguson
 */
public class CatalogModule extends AbstractModule {

    @Override
    protected void configure() {

        // one eventbus to rule them all, and in the darkness bind them
        bind(EventBus.class).in(Singleton.class);

    }

    @Provides
    ConfigMap provideConfig() {
        ConfigMap config = new ConfigMap();
        config.putAll(new ConfigParser("config.xml").getConfig());
        return config;
    }

}
