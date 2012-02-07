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
package org.onexus.task.executor.loader.tsv;

import org.onexus.core.IEntity;
import org.onexus.core.resources.Collection;
import org.onexus.core.resources.Field;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class FileEntity implements IEntity {

    private long position;
    private Collection collection;

    private String line;
    private Map<String, Integer> headers;
    private Map<String, Field> fields;

    protected String NULL_CHAR = "-";
    protected String SEPARATOR = "\t";

    public FileEntity(Collection collection, String line, long position) {
        this.collection = collection;
        this.position = position;

        this.fields = new HashMap<String, Field>();
        for (Field field : collection.getFields()) {
            this.fields.put(field.getName(), field);
        }
    }

    @Override
    public String getId() {
        return Long.toHexString(position);
    }

    protected long getPosition() {
        return position;
    }

    protected void setPosition(long position) {
        this.position = position;
    }

    @Override
    public Collection getCollection() {
        return collection;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public Object get(String fieldURI) {

        if (!fields.containsKey(fieldURI)) {
            return null;
        }

        Field field = fields.get(fieldURI);
        String value = null;
        if (headers.containsKey(field.getName())) {
            Integer position = headers.get(field.getName());
            value = parseField(line, position);
        }

        if (value == null) {
            return null;
        } else {

            // Remove "
            value = value.replace('"', ' ');

            // Trim blank spaces
            value = value.trim();

        }

        try {

            Class<?> fieldClass = field.getDataType();

            // TODO use adapter factory as in SQL manager
            if (fieldClass.equals(Boolean.class)) {
                return new Boolean((value.trim().equalsIgnoreCase("1")));
            }

            // For number types return null if the value is empty
            if (Number.class.isAssignableFrom(fieldClass)) {
                if (value.equals("")) {
                    return null;
                }
            }

            Constructor<?> constructor = fieldClass
                    .getConstructor(String.class);

            return constructor.newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("The value '" + value
                    + "' for the field '" + fieldURI
                    + "' is malformed on line '" + line + "'", e);
        }

    }

    @Override
    public void put(String fieldURI, Object value) {
        throw new UnsupportedOperationException("Read-only FileEntity");
    }

    public Map<String, Integer> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Integer> headers) {
        this.headers = headers;
    }

    protected String parseField(String str, int num) {

        int start = -1;
        for (int i = 0; i < num; i++) {
            start = str.indexOf(SEPARATOR, start + 1);
            if (start == -1)
                return null;
        }

        int end = str.indexOf(SEPARATOR, start + 1);
        if (end == -1)
            end = str.length();

        String result = str.substring(start + 1, end);

        if (result != null && result.equals(NULL_CHAR)) {
            return null;
        }

        return result.replace('"', ' ').trim();

    }

}
