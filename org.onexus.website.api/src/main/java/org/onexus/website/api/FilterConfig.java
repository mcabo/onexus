/**
 *  Copyright 2012 Universitat Pompeu Fabra.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.onexus.website.api;

import org.onexus.resource.api.ORI;
import org.onexus.resource.api.annotations.ResourceAlias;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ResourceAlias("filter-config")
public class FilterConfig implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private ORI collection;

    @NotNull
    private String define;

    @NotNull
    private String where;

    private boolean deletable = false;

    private transient boolean enable = true;

    public FilterConfig() {
        super();
    }

    public FilterConfig(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ORI getCollection() {
        return collection;
    }

    public void setCollection(ORI collection) {
        this.collection = collection;
    }

    public String getDefine() {
        return define;
    }

    public void setDefine(String define) {
        this.define = define;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterConfig that = (FilterConfig) o;

        if (!collection.equals(that.collection)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + collection.hashCode();
        return result;
    }
}