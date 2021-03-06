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
package org.onexus.website.widget.search;

import org.onexus.resource.api.ORI;
import org.onexus.resource.api.annotations.ResourceAlias;

import java.io.Serializable;

@ResourceAlias("search-field")
public class SearchField implements Serializable {

    private ORI collection;

    private String fields;

    public SearchField() {
        super();
    }

    public SearchField(ORI collectionURI, String fields) {
        super();
        this.collection = collectionURI;
        this.fields = fields;
    }

    public ORI getCollection() {
        return collection;
    }

    public void setCollection(ORI collectionURI) {
        this.collection = collectionURI;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

}
