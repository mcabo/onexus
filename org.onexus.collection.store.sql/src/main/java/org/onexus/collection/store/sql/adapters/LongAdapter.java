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
package org.onexus.collection.store.sql.adapters;

import java.sql.ResultSet;

public class LongAdapter extends SqlAdapter {

    public LongAdapter() {
        super(Long.class);
    }

    @Override
    public void append(StringBuilder container, Object object) throws Exception {
        NumericTools.toHexString(container, ((Long) object).longValue());
    }

    @Override
    public Object extract(ResultSet container, Object... parameters)
            throws Exception {
        long value = container.getLong((String) parameters[0]);
        if (container.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

}
