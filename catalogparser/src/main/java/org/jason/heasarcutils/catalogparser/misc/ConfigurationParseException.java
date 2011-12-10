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
package org.jason.heasarcutils.catalogparser.misc;

/**
 * Since I can't figure out an appropriate Exception to throw if something goes wrong during the
 * config parsing, I'll roll my own. Yeah. Great solution *eye roll*.
 *
 * @since 0.1
 * @author Jason Ferguson
 */
public class ConfigurationParseException extends RuntimeException {

    public ConfigurationParseException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurationParseException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurationParseException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurationParseException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
