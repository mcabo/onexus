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
package org.onexus.website.widget.map;

import org.onexus.resource.api.ORI;
import org.onexus.resource.api.annotations.ResourceAlias;
import org.onexus.website.api.widget.WidgetConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ResourceAlias("widget-map")
public class MapWidgetConfig extends WidgetConfig {

    @Valid
    private MapWidgetStatus defaultStatus;

    private String warning;

    @NotNull
    private ORI collection;

    public MapWidgetConfig() {
        super();
    }

    public MapWidgetConfig(String id) {
        super(id);
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public ORI getCollection() {
        return collection;
    }

    public void setCollection(ORI collection) {
        this.collection = collection;
    }

    @Override
    public MapWidgetStatus createEmptyStatus() {
        return new MapWidgetStatus(getId());
    }

    public MapWidgetStatus getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(MapWidgetStatus defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

}
