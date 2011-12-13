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
package org.jason.heasarcutils.catalogparser.util;

import java.io.Serializable;
import java.util.*;

/**
 * Represents configuration data for a single astronomical catalog
 *
 * @since 0.1
 * @author Jason Ferguson
 */
public class Catalog implements Serializable {

    private String name;
    private String type;
    private String title;
    private String description;
    private String url;
    private String headerUrl;
    private String epoch;

    private Map<String, FieldData> fieldData = new LinkedHashMap<String, FieldData>();
    private Set<FieldData> fieldDataSet = new TreeSet<FieldData>(new FieldDataStartFieldComparator());

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeaderUrl() {
        return headerUrl;
    }

    public void setHeaderUrl(String headerUrl) {
        this.headerUrl = headerUrl;
    }

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public Map<String, FieldData> getFieldData() {
        return fieldData;
    }

    public void setFieldData(Map<String, FieldData> fieldData) {
        this.fieldData = fieldData;
    }

    public Set<FieldData> getFieldDataSet() {
        return fieldDataSet;
    }

    public void setFieldDataSet(Set<FieldData> fieldDataSet) {
        this.fieldDataSet = fieldDataSet;
    }

}
