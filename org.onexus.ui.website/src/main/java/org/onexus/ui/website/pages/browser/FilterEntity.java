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
package org.onexus.ui.website.pages.browser;

import org.apache.wicket.markup.html.panel.Panel;
import org.h2.util.StringUtils;
import org.onexus.collection.api.ICollectionManager;
import org.onexus.collection.api.IEntity;
import org.onexus.resource.api.IResourceManager;
import org.onexus.collection.api.query.EqualId;
import org.onexus.collection.api.query.Filter;
import org.onexus.collection.api.query.Query;
import org.onexus.collection.api.Collection;
import org.onexus.collection.api.Field;
import org.onexus.collection.api.utils.EntityIterator;
import org.onexus.collection.api.utils.QueryUtils;
import org.onexus.resource.api.utils.ResourceUtils;
import org.onexus.ui.api.OnexusWebApplication;
import org.onexus.ui.website.pages.browser.boxes.GenericBox;
import org.onexus.ui.website.utils.EntityModel;
import org.onexus.ui.website.utils.SingleEntityQuery;
import org.onexus.ui.website.utils.visible.VisibleRule;
import org.onexus.ui.website.widgets.filters.FilterConfig;

import javax.inject.Inject;
import java.util.regex.Pattern;

public class FilterEntity implements IFilter {

    private String filteredCollection;
    private String entityId;
    private boolean deletable;
    private boolean enable;

    @Inject
    private IResourceManager resourceManager;

    @Inject
    private ICollectionManager collectionManager;

    public FilterEntity() {
        super();
        OnexusWebApplication.inject(this);
    }

    public FilterEntity(IEntity entity) {
        this(entity, true);
    }

    public FilterEntity(IEntity entity, boolean deletable) {
        this(entity.getCollection().getURI(), entity.getId(), deletable);
    }

    public FilterEntity(String filteredCollection, String entityId) {
        this(filteredCollection, entityId, true);
    }

    public FilterEntity(String filteredCollection, String entityId, boolean deletable) {
        super();
        OnexusWebApplication.inject(this);

        this.filteredCollection = filteredCollection;
        this.entityId = entityId;
        this.deletable = deletable;
        this.enable = true;
    }

    @Override
    public String getFilteredCollection() {
        return filteredCollection;
    }

    @Override
    public FilterConfig getFilterConfig() {
        //TODO
        return null;
    }

    public void setFilteredCollection(String filteredCollection) {
        this.filteredCollection = filteredCollection;
    }

    @Override
    public String getVisible() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }

    @Override
    public boolean isDeletable() {
        return deletable;
    }

    @Override
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public Filter buildFilter(Query query) {
        String collectionAlias = QueryUtils.newCollectionAlias(query, filteredCollection);
        return new EqualId(collectionAlias, entityId);
    }

    @Override
    public boolean isVisible(VisibleRule rule) {

        boolean negated = rule.isNegated();

        boolean validCollection = (filteredCollection.endsWith(rule.getFilteredCollection()) ? !negated : negated);

        if (rule.getOperator() == null) {
            return validCollection;
        }

        String collectionUri = ResourceUtils.getAbsoluteURI(rule.getParentURI(), filteredCollection);
        IEntity entity = (new EntityModel(collectionUri, entityId)).getObject();

        String fieldValue = String.valueOf(entity.get(rule.getField()));

        return (StringUtils.equals(fieldValue , rule.getValue()) ? !negated : negated);
    }

    @Override
    public String toUrlParameter() {
        return filteredCollection + "::" + entityId + "::" + (deletable?"d":"") + (enable?"e":"");
    }

    @Override
    public void loadUrlPrameter(String parameter) {
        String[] values = parameter.split("::");

        this.filteredCollection = values[0];
        this.entityId = values[1];
        String flags = values[2];
        deletable = flags.contains("d");
        enable = flags.contains("e");
    }


    @Override
    public String getLabel(Query query) {

        // Make collection URI absolute
        String filteredCollection = QueryUtils.getAbsoluteCollectionUri(query, getFilteredCollection());
        Collection collection = resourceManager.load(Collection.class, filteredCollection);

        String collectionLabel = collection.getProperty("FIXED_COLLECTION_LABEL");
        if (collectionLabel == null) {
            collectionLabel = collection.getName();
        }

        return collectionLabel;

    }

    @Override
    public String getTitle(Query query) {

        // Make collection URI absolute
        String filteredCollection = QueryUtils.getAbsoluteCollectionUri(query, getFilteredCollection());
        Collection collection = resourceManager.load(Collection.class, filteredCollection);

        String entityField = collection.getProperty("FIXED_ENTITY_FIELD");
        String entityLabel = getEntityId();
        if (entityField != null) {
            IEntity entity = new EntityIterator(collectionManager.load(new SingleEntityQuery(filteredCollection, getEntityId())), filteredCollection).next();

            entityLabel = String.valueOf(entity.get(entityField));

            if (entityLabel == null || entityLabel.isEmpty()) {
                entityLabel = getEntityId();
            }

        }

        String entityPattern = collection.getProperty("FIXED_ENTITY_PATTERN");
        if (entityPattern != null) {
            IEntity entity = new EntityIterator(collectionManager.load(new SingleEntityQuery(filteredCollection, getEntityId())), filteredCollection).next();

            if (entity != null) {
                entityLabel = parseTemplate(entityPattern, entity);
            }
        }

        return entityLabel;
    }

    @Override
    public Panel getTooltip(String componentId, Query query) {
        String filteredCollection = QueryUtils.getAbsoluteCollectionUri(query, getFilteredCollection());
        return new GenericBox(componentId, new EntityModel(filteredCollection, entityId));
    }

    private String parseTemplate(String entityPattern, IEntity entity) {


        for (Field field : entity.getCollection().getFields()) {
            String fieldName = field.getId();
            entityPattern = entityPattern.replaceAll(
                    Pattern.quote("${" + fieldName + "}"),
                    String.valueOf(entity.get(fieldName))
            );
        }

        return entityPattern;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FixedEntity [collectionId=");
        builder.append(filteredCollection);
        builder.append(", entityId=");
        builder.append(entityId);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((filteredCollection == null) ? 0 : filteredCollection.hashCode());
        result = prime * result
                + ((entityId == null) ? 0 : entityId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FilterEntity other = (FilterEntity) obj;
        if (filteredCollection == null) {
            if (other.filteredCollection != null)
                return false;
        } else if (!filteredCollection.equals(other.filteredCollection))
            return false;
        if (entityId == null) {
            if (other.entityId != null)
                return false;
        } else if (!entityId.equals(other.entityId))
            return false;
        return true;
    }



}