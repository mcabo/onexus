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
package org.onexus.collection.api.query;


import java.io.Serializable;
import java.sql.Date;

public abstract class Filter implements Serializable {

    public Filter() {
        super();
    }

    public String toString() {
        return toString(new StringBuilder(), true).toString();
    }

    public abstract StringBuilder toString(StringBuilder oql, boolean prettyPrint);


    public static String convertToOQL(Object value) {
        return Query.escapeString(value.toString());
    }

    public static Long convertToLong(String oqlValue) {
        return oqlValue == null ? null : Long.decode(oqlValue);
    }

    public static String convertToString(String oqlValue) {
        return oqlValue == null ? null : oqlValue;
    }

    public static Date convertToDate(String oqlValue) {
        return oqlValue == null ? null : Date.valueOf(oqlValue);
    }

    public static Double convertToDouble(String oqlValue) {
        return oqlValue == null ? null : Double.valueOf(oqlValue);
    }

    public static String endingTabs(StringBuilder oql) {
        StringBuilder prevTabs = new StringBuilder();
        int l = oql.length();
        for (int i = l - 1; oql.charAt(i) == '\t' && i > 0; i--) {
            prevTabs.append('\t');
        }

        return prevTabs.toString();
    }

}
