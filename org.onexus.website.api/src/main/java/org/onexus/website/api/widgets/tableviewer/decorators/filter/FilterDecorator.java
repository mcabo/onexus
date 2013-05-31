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
package org.onexus.website.api.widgets.tableviewer.decorators.filter;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.onexus.collection.api.Field;
import org.onexus.collection.api.IEntity;
import org.onexus.resource.api.ORI;
import org.onexus.website.api.Website;
import org.onexus.website.api.events.AbstractEvent;
import org.onexus.website.api.events.EventAddFilter;
import org.onexus.website.api.pages.browser.BrowserPageLink;
import org.onexus.website.api.pages.browser.BrowserPageStatus;
import org.onexus.website.api.pages.browser.FilterEntity;
import org.onexus.website.api.widgets.tableviewer.decorators.utils.FieldDecorator;
import org.onexus.website.api.widgets.tableviewer.decorators.utils.LinkPanel;

public class FilterDecorator extends FieldDecorator {

    private ORI collectionId;

    public FilterDecorator(ORI collectionId, Field field) {
        super(field);
        this.collectionId = collectionId;
    }

    @Override
    public void populateCell(WebMarkupContainer cellContainer, String componentId, IModel<IEntity> data) {
        Object value = getValue(data.getObject());

        String label = "<i class=\"icon-hand-up\"></i>";
        String tooltip = "Filter results by " + getField().getLabel() + " = " + String.valueOf(value);

        LinkPanel linkPanel = new LinkPanel(componentId, label, getLink(collectionId, data, tooltip));

        if (value == null) {
            linkPanel.setVisible(false);
        }

        cellContainer.add(linkPanel);
    }

    protected WebMarkupContainer getLink(ORI collectionId, IModel<IEntity> data, String tooltip) {

        String entityId = null;
        IEntity entity = data.getObject();

        if (entity.getCollection().getORI().equals(collectionId)) {
            entityId = entity.getId();
        } else {
            entityId = String.valueOf(entity.get(getField().getId()));
        }

        WebMarkupContainer link = new BrowserPageLink<FilterEntity>(LinkPanel.LINK_ID, Model.of(new FilterEntity(collectionId, entityId))) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                BrowserPageStatus status = getBrowserPageStatus();
                FilterEntity rowEntity = getModelObject();

                // Try to make the link relative to the current website project
                Website website = findParent(Website.class);
                if (website != null) {
                    ORI projectUri = website.getConfig().getORI().getParent();
                    ORI relativeUri = rowEntity.getFilteredCollection().toRelative(projectUri);
                    rowEntity.setFilteredCollection(relativeUri);
                }

                AbstractEvent[] events = FilterDecorator.this.onClick(rowEntity, status);
                for (AbstractEvent event : events) {
                    sendEvent(event);
                }
            }

        };

        link.add(new AttributeModifier("rel", "tooltip"));
        link.add(new AttributeModifier("title", tooltip));
        link.add(new AttributeModifier("data-placement", "right"));
        return link;
    }

    protected AbstractEvent[] onClick(FilterEntity rowEntity, BrowserPageStatus status) {

        // Fix current row entity
        status.addFilter(rowEntity);

        return new AbstractEvent[]{EventAddFilter.EVENT};
    }


}
