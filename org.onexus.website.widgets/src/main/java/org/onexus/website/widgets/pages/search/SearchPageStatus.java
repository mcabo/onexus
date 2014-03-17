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
package org.onexus.website.widgets.pages.search;

import org.onexus.website.api.widgets.WidgetStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SearchPageStatus extends WidgetStatus<SearchPageConfig> {

    @Valid
    private SearchType type;

    @NotNull
    private String search;

    public SearchPageStatus() {
    }

    public SearchPageStatus(String widgetId) {
        super(widgetId);
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        this.type = type;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
