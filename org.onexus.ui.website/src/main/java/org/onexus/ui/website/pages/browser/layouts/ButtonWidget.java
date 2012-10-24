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
package org.onexus.ui.website.pages.browser.layouts;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.onexus.ui.website.events.AbstractEvent;
import org.onexus.ui.website.events.EventPanel;
import org.onexus.ui.website.events.EventQueryUpdate;
import org.onexus.ui.website.pages.browser.BrowserPageStatus;
import org.onexus.ui.website.widgets.*;

import javax.inject.Inject;


public class ButtonWidget extends EventPanel {

    @Inject
    public transient IWidgetManager widgetManager;

    private WidgetConfig widgetConfig;
    private IModel<BrowserPageStatus> pageModel;

    public ButtonWidget(String id, final WidgetConfig widgetConfig, final IModel<BrowserPageStatus> pageModel) {
        super(id);
        onEventFireUpdate(EventQueryUpdate.class);

        this.widgetConfig = widgetConfig;
        this.pageModel = pageModel;

        final WebMarkupContainer widgetModal = new WebMarkupContainer("widgetModal");
        widgetModal.setOutputMarkupId(true);
        widgetModal.add(new EmptyPanel("widget"));

        widgetModal.add(new AjaxLink<String>("close") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                Component widget = widgetModal.get("widget");
                if (widget instanceof Widget) {
                    ((Widget) widget).onClose(target);
                }
                target.appendJavaScript("$('#" + widgetModal.getMarkupId() + "').modal('hide')");
            }
        });

        add(widgetModal);


        Label button = new Label("button", new PropertyModel<String>(this, "buttonText"));
        button.setEscapeModelStrings(false);
        button.setOutputMarkupId(true);

        if (widgetConfig.getTitle() != null) {
            button.add(new AttributeModifier("title", widgetConfig.getTitle()));
            button.add(new AttributeModifier("rel", "tooltip"));
            widgetModal.add(new Label("modalHeader", widgetConfig.getTitle()));
        } else {
            widgetModal.add(new Label("modalHeader", ""));
        }

        button.add(new AjaxEventBehavior("onclick") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                Widget<?, ?> widgetPanel = widgetManager.create("widget", new WidgetModel(widgetConfig.getId(), pageModel));
                widgetModal.addOrReplace(widgetPanel);
                target.add(widgetModal);
                target.appendJavaScript("$('#" + widgetModal.getMarkupId() + "').modal('show')");
            }
        });

        add(button);


    }

    public String getButtonText() {
        WidgetStatus status = pageModel.getObject().getWidgetStatus(widgetConfig.getId());
        String buttonText = widgetConfig.getButton();
        if (status != null) {
            buttonText = status.getButton();
        }
        return buttonText;
    }

    @Override
    protected void onRegisteredEvent(AjaxRequestTarget target, AbstractEvent event) {
        target.add( get("button"));
    }
}
