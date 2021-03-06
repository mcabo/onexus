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
package org.onexus.website.widget.tableviewer;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.PropertyModel;
import org.onexus.website.api.events.EventViewChange;

import java.util.Arrays;
import java.util.List;

public class NavigationToolbar extends AbstractToolbar {

    private Integer rowsPerPage;

    private static List<Integer> PAGE_OPTIONS = Arrays.asList(20, 40, 60, 100);

    public NavigationToolbar(final DataTable<?, ?> table) {
        super(table);


    }

    @Override
    protected void onBeforeRender() {

        WebMarkupContainer span = new WebMarkupContainer("span");
        addOrReplace(span);
        span.add(AttributeModifier.replace("colspan", String.valueOf(getTable().getColumns().size())));

        DropDownChoice<Integer> dropDown = new DropDownChoice<Integer>("rowsxpage",
                new PropertyModel<Integer>(this, "rowsPerPage"),
                PAGE_OPTIONS,
                new RowsPerPageChoiceRenderer()
        );

        dropDown.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                send(getPage(), Broadcast.BREADTH, EventViewChange.EVENT);
            }
        });

        span.add(dropDown);

        span.add(new PrevLink("prev"));

        span.add(new NextLink("next"));

        AjaxLink<String> countLink = new IndicatingAjaxLink<String>("count") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                getDataProvider().forceCount();
                target.add(getTable());
            }
        };
        span.add(countLink);
        countLink.add(new Label("from", new PropertyModel<Long>(this, "from")));
        countLink.add(new Label("to", new PropertyModel<Long>(this, "to")));
        countLink.add(new Label("of", new PropertyModel<String>(this, "of")));

        super.onBeforeRender();
    }

    public long getFrom() {

        if (getDataProvider().getRealSize() == 0) {
            return 0;
        }

        long itemsPerPage = getTable().getItemsPerPage();
        long currentPage = getTable().getCurrentPage();
        return (currentPage * itemsPerPage) + 1;
    }

    public long getTo() {
        long itemsPerPage = getTable().getItemsPerPage();
        long from = getFrom();
        return from + Math.min(itemsPerPage, getDataProvider().size() - from + 1) - 1;
    }

    public String getOf() {

        long realSize = getDataProvider().getRealSize();

        if (realSize == -1) {
            return "(click to count)";
        }

        return Long.toString(realSize);
    }

    private EntitiesRowProvider getDataProvider() {
        return (EntitiesRowProvider) getTable().getDataProvider();
    }

    public Integer getRowsPerPage() {

        if (rowsPerPage == null) {
            Integer sessionRowsPerPage = getSession().getMetaData(TableViewer.DEFAULT_ROWS_PER_PAGE);
            rowsPerPage = sessionRowsPerPage == null ? 20 : sessionRowsPerPage;
        }

        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        getSession().setMetaData(TableViewer.DEFAULT_ROWS_PER_PAGE, rowsPerPage);
        this.rowsPerPage = rowsPerPage;
    }

    private class PrevLink extends AjaxLink<String> {

        public PrevLink(String id) {
            super(id);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            getTable().setCurrentPage(getTable().getCurrentPage() - 1);
            target.add(getTable());
        }

        @Override
        protected void onBeforeRender() {
            setVisible(getFrom() > 1);
            super.onBeforeRender();
        }

    }

    private class NextLink extends AjaxLink<String> {

        public NextLink(String id) {
            super(id);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {

            long page = getTable().getCurrentPage() + 1;
            getTable().setCurrentPage(page);

            getDataProvider().setKnownSize(((page + 1) * getTable().getItemsPerPage()) + 2);

            target.add(getTable());

        }

        @Override
        protected void onBeforeRender() {
            long to = getTo();
            long size = getDataProvider().size();

            setVisible(size > to);
            super.onBeforeRender();
        }
    }

    private class RowsPerPageChoiceRenderer implements IChoiceRenderer<Integer> {

        @Override
        public Object getDisplayValue(Integer object) {
            return String.valueOf(object) + " rows";
        }

        @Override
        public String getIdValue(Integer object, int index) {
            return String.valueOf(object);
        }
    }

}
