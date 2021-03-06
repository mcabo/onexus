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
package org.onexus.website.widget.selector;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.onexus.collection.api.query.EqualId;
import org.onexus.collection.api.query.Query;
import org.onexus.collection.api.utils.QueryUtils;
import org.onexus.resource.api.ORI;
import org.onexus.website.api.widget.WidgetStatus;

public class SelectorWidgetStatus extends WidgetStatus<SelectorWidgetConfig> {

    private String selection;

    public SelectorWidgetStatus() {
        super();
    }

    public SelectorWidgetStatus(String widgetId) {
        super(widgetId);
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    @Override
    public void onQueryBuild(Query query) {

        if (selection != null) {
            SelectorWidgetConfig config = getConfig();

            if (config.getSelection() == null || !config.getSelection()) {
                String collectionAlias = QueryUtils.newCollectionAlias(query, config.getCollection());
                QueryUtils.and(query, new EqualId(collectionAlias, selection));

                ORI mapCollection = config.getMapCollection();
                if (mapCollection != null) {
                    QueryUtils.newCollectionAlias(query, mapCollection);
                }
            }
        }

    }

    @Override
    public void decodeParameters(PageParameters parameters, String keyPrefix) {
        StringValue s = parameters.get(keyPrefix + "s");

        if (!s.isEmpty()) {
            selection = s.toString();
        }
    }

    @Override
    public void encodeParameters(PageParameters parameters, String keyPrefix) {
        parameters.set(keyPrefix + "s", selection);
    }
}
