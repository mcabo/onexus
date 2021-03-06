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

import org.apache.wicket.model.IModel;
import org.onexus.website.api.widget.AbstractWidgetCreator;
import org.onexus.website.api.widget.Widget;

public class SearchWidgetCreator extends AbstractWidgetCreator<SearchWidgetConfig, SearchWidgetStatus> {

    public SearchWidgetCreator() {
        super(SearchWidgetConfig.class, "search-widget", "Search widget");
    }

    @Override
    protected Widget<?, ?> build(String componentId, IModel<SearchWidgetStatus> statusModel) {
        return new SearchWidget(componentId, statusModel);
    }

}
