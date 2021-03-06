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

import org.onexus.collection.api.query.Contains;
import org.onexus.collection.api.query.Filter;
import org.onexus.collection.api.query.Query;
import org.onexus.collection.api.utils.QueryUtils;
import org.onexus.website.api.widget.WidgetStatus;

import java.util.ArrayList;
import java.util.List;

public class SearchWidgetStatus extends WidgetStatus<SearchWidgetConfig> {

    private String search;

    public SearchWidgetStatus() {
        super();
    }

    public SearchWidgetStatus(String widgetId) {
        super(widgetId);
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    @Override
    public void onQueryBuild(Query query) {

        if (search != null) {

            SearchWidgetConfig config = getConfig();

            List<Filter> filters = new ArrayList<Filter>();

            for (SearchField searchField : config.getFields()) {

                String collectionAlias = QueryUtils.newCollectionAlias(query, searchField.getCollection());
                String fields[] = searchField.getFields().split(",");

                for (String field : fields) {
                    filters.add(new Contains(collectionAlias, field.trim(), search));
                }
            }

            QueryUtils.and(query, QueryUtils.joinOr(filters));

        }

    }

}
