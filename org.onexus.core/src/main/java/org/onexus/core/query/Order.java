/**
 *  Copyright 2011 Universitat Pompeu Fabra.
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
package org.onexus.core.query;

import java.io.Serializable;

public class Order implements Serializable {

    private String collection;
    private String fieldName;
    private boolean ascending;

    public Order() {
        super();
    }

    public Order(String collectionURI, String fieldName, boolean ascending) {
        super();
        this.collection = collectionURI;
        this.fieldName = fieldName;
        this.ascending = ascending;
    }

    public String getCollection() {
        return collection;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isAscending() {
        return ascending;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Order [collectionURI=");
        builder.append(collection);
        builder.append(", fieldName=");
        builder.append(fieldName);
        builder.append(", ascending=");
        builder.append(ascending);
        builder.append("]");
        return builder.toString();
    }

}
