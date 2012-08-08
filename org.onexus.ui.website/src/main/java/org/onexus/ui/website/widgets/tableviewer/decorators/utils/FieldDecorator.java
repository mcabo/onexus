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
package org.onexus.ui.website.widgets.tableviewer.decorators.utils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.onexus.collection.api.IEntity;
import org.onexus.collection.api.Field;
import org.onexus.collection.api.types.Text;
import org.onexus.ui.website.widgets.tableviewer.decorators.IDecorator;
import org.onexus.ui.website.widgets.tableviewer.formaters.DoubleFormater;
import org.onexus.ui.website.widgets.tableviewer.formaters.ITextFormater;
import org.onexus.ui.website.widgets.tableviewer.formaters.StringFormater;

/**
 * Simple field decorator using a ITextFormater.
 *
 * @author Jordi Deu-Pons
 */
public class FieldDecorator implements IDecorator {

    private Field field;
    private ITextFormater textFormater;
    private String cssClass;

    /**
     * @param field The field to use to show the value.
     */
    public FieldDecorator(Field field) {
        this(field, null, null);
    }

    /**
     * @param field    The field to use to show the value.
     * @param cssClass The CSS class of the cell.
     */
    public FieldDecorator(Field field, String cssClass) {
        this(field, null, cssClass);
    }

    public FieldDecorator(Field field, ITextFormater textFormater) {
        this(field, textFormater, null);
    }

    public FieldDecorator(Field field, ITextFormater textFormater,
                          String cssClass) {
        super();
        this.field = field;
        this.textFormater = (textFormater == null ? getTextFormater(field) : textFormater);
        this.cssClass = cssClass;
    }

    protected Object getValue(IEntity data) {
        return (data == null ? null : data.get(field.getId()));
    }

    public String getFormatValue(final IEntity entity) {

        Object value = getValue(entity);

        if (textFormater != null) {
            return textFormater.format(value);
        }

        if (value == null) {
            return "";
        }

        return value.toString();

    }

    @Override
    public String getColor(final IEntity data) {
        return "#000000";
    }

    @Override
    public void detach() {
    }

    @Override
    public void populateCell(WebMarkupContainer cellContainer,
                             String componentId, IModel<IEntity> data) {
        Object value = getValue(data.getObject());
        cellContainer.add(new Label(componentId, getFormatValue(data
                .getObject())));
        cellContainer.add(new AttributeModifier("title", new Model<String>(
                (value == null ? "No data" : value.toString()))));

        if (cssClass != null) {
            cellContainer.add(new AttributeModifier("class", new Model<String>(
                    cssClass)));
        }
    }

    public Field getField() {
        return field;
    }

    @Deprecated
    private static ITextFormater getTextFormater(Field field) {
        if (field.getType().equals(String.class)) {
            int maxLength;
            String value = field.getProperty("MAX_LENGTH");

            try {
                maxLength = Integer.valueOf(value);
            } catch (Exception e) {
                maxLength = 20;
            }

            return new StringFormater(maxLength, true);
        }

        if (field.getType().equals(Text.class)) {
            return new StringFormater(50, true);
        }

        if (field.getType().equals(Double.class)
                || field.getType().equals(Long.class)
                || field.getType().equals(Integer.class)) {
            return DoubleFormater.INSTANCE;
        }

        return null;
    }

}