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
package org.onexus.website.widget.text;

import org.onexus.resource.api.annotations.ResourceAlias;
import org.onexus.website.api.widget.WidgetConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ResourceAlias("widget-text")
public class TextWidgetConfig extends WidgetConfig {

    @Valid
    private TextWidgetStatus defaultStatus;

    @NotNull
    private String title;

    @NotNull
    private String text;

    private String details;

    public TextWidgetConfig() {
        super();
    }

    public TextWidgetConfig(String id, String title, String text) {
        super(id);

        this.title = title;
        this.text = text;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public TextWidgetStatus createEmptyStatus() {
        return new TextWidgetStatus(getId());
    }

    public TextWidgetStatus getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(TextWidgetStatus defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

}
