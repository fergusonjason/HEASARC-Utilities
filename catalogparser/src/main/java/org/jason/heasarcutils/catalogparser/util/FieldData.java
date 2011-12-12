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

/**
 * @since 0.1
 * @author Jason Ferguson
 */
public class FieldData implements Serializable, Comparable<FieldData>{

    private String name = "noname";
    private String renameTo;
    private boolean keepAfterCopy = false;
    private String prefix;
    private int start;
    private int end;
    private boolean included = false;

    public FieldData() {
    }

    public FieldData(boolean excluded) {
        this.included = excluded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRenameTo() {
        return renameTo;
    }

    public void setRenameTo(String renameTo) {
        this.renameTo = renameTo;
    }

    public boolean isKeepAfterCopy() {
        return keepAfterCopy;
    }

    public void setKeepAfterCopy(boolean keepAfterCopy) {
        this.keepAfterCopy = keepAfterCopy;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldData fieldData = (FieldData) o;

        if (!name.equals(fieldData.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(FieldData o) {
        return this.getName().compareTo(o.getName());
    }
}
