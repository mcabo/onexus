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
package org.onexus.website.widget.browser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.onexus.website.api.Website;
import org.onexus.website.api.events.EventAddFilter;
import org.onexus.website.api.events.EventRemoveFilter;
import org.onexus.website.api.events.EventTabSelected;
import org.onexus.website.api.events.EventViewChange;
import org.onexus.website.api.utils.visible.VisiblePredicate;
import org.onexus.website.api.widget.Widget;
import org.onexus.website.widget.browser.layouts.leftmain.LeftMainLayout;
import org.onexus.website.widget.browser.layouts.single.SingleLayout;
import org.onexus.website.widget.browser.layouts.topleft.TopleftLayout;
import org.onexus.website.widget.browser.layouts.topmain.TopmainLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowserPage extends Widget<BrowserPageConfig, BrowserPageStatus> {

    public BrowserPage(String componentId, IModel<BrowserPageStatus> statusModel) {
        super(componentId, statusModel);
        onEventFireUpdate(EventTabSelected.class, EventAddFilter.class, EventRemoveFilter.class);

        add(new SelectionPanel("selection", statusModel));

        onEventFireUpdate(EventViewChange.class);
    }

    protected boolean isCurrentTab(String tabId) {
        return tabId.equals(getStatus().getCurrentTabId());
    }

    protected TabConfig getCurrentTab() {
        String currentTabId = getStatus().getCurrentTabId();
        return getConfig().getTab(currentTabId);
    }

    @Override
    protected void onBeforeRender() {

        VisibleTabs visibleTabs = new VisibleTabs();

        // Tabs can change when we fix/unfix entities
        addOrReplace(new ListView<TabGroup>("tabs", visibleTabs) {

            @Override
            protected void populateItem(ListItem<TabGroup> item) {

                TabGroup tabGroup = item.getModelObject();

                if (tabGroup.hasSubMenu()) {

                    WebMarkupContainer link = new WebMarkupContainer("link");
                    link.add(new AttributeModifier("class", "dropdown-toggle"));
                    link.add(new AttributeModifier("data-toggle", "dropdown"));
                    link.add(new AttributeModifier("href", "#"));

                    if (tabGroup.containsTab(getStatus().getCurrentTabId())) {
                        item.add(new AttributeModifier("class", "dropdown active"));

                        TabConfig currentTab = getConfig().getTab(getStatus().getCurrentTabId());
                        String tabTitle = StringUtils.abbreviate(currentTab.getTitle(), 14);
                        String currentLabel = "<div style=\"position:relative;\"><div style=\"white-space: nowrap; position:absolute; left:13px; top: 13px; font-size: 9px;\"><em>" + tabTitle + "</em></div></div>";
                        link.add(new Label("label", currentLabel + "<b class='caret'></b>&nbsp;" + tabGroup.getGroupLabel()).setEscapeModelStrings(false));

                    } else {
                        item.add(new AttributeModifier("class", "dropdown"));
                        link.add(new Label("label", "<b class='caret'></b>&nbsp;" + tabGroup.getGroupLabel()).setEscapeModelStrings(false));
                    }
                    item.add(link);


                    WebMarkupContainer submenu = new WebMarkupContainer("submenu");
                    submenu.add(new ListView<TabConfig>("item", tabGroup.getTabConfigs()) {

                        @Override
                        protected void populateItem(ListItem<TabConfig> item) {

                            TabConfig tab = item.getModelObject();


                            BrowserPageLink<String> link = new BrowserPageLink<String>("link", Model.of(tab.getId())) {

                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    getStatus().setCurrentTabId(getModelObject());
                                    sendEvent(EventTabSelected.EVENT);
                                }

                            };

                            if (!Strings.isEmpty(tab.getHelp())) {
                                item.add(new AttributeModifier("rel", "tooltip"));
                                item.add(new AttributeModifier("data-placement", "right"));
                                item.add(new AttributeModifier("title", tab.getHelp()));
                            }

                            link.add(new Label("label", tab.getTitle()));
                            item.add(link);

                            if (isCurrentTab(tab.getId())) {
                                item.add(new AttributeModifier("class", new Model<String>("active")));
                            }

                        }
                    });
                    item.add(submenu);

                } else {

                    TabConfig tab = tabGroup.getTabConfigs().get(0);
                    BrowserPageLink<String> link = new BrowserPageLink<String>("link", Model.of(tab.getId())) {

                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            getStatus().setCurrentTabId(getModelObject());
                            sendEvent(EventTabSelected.EVENT);
                        }

                    };

                    if (!Strings.isEmpty(tab.getHelp())) {
                        link.add(new AttributeModifier("rel", "tooltip"));
                        link.add(new AttributeModifier("title", tab.getHelp()));
                    }

                    link.add(new Label("label", tab.getTitle()));
                    item.add(link);

                    if (isCurrentTab(tab.getId())) {
                        item.add(new AttributeModifier("class", new Model<String>("active")));
                    }

                    item.add(new WebMarkupContainer("submenu").setVisible(false));

                }

            }

        });

        // Check if the current selected tab is visible.
        String currentTabId = getStatus().getCurrentTabId();

        // Set a current tab if there is no one.
        if (currentTabId == null) {

            List<TabConfig> tabs = getConfig().getTabs();
            if (tabs != null && !tabs.isEmpty()) {
                currentTabId = tabs.get(0).getId();
                getStatus().setCurrentTabId(currentTabId);

                // Init currentPage
                //TODO This is not a good solution
                PageParameters pageParameters = getPage().getPageParameters();
                pageParameters.set(Website.PARAMETER_CURRENT_PAGE, getConfig().getId());
                pageParameters.set("ptab", currentTabId);
                setResponsePage(Website.class, pageParameters);
            }

        }

        List<TabGroup> tabs = visibleTabs.getObject();
        boolean hiddenTab = true;
        for (TabGroup tab : tabs) {
            if (tab.containsTab(currentTabId)) {
                hiddenTab = false;
            }
        }

        if (hiddenTab && !tabs.isEmpty()) {
            TabConfig firstTab = tabs.get(0).getTabConfigs().get(0);
            getStatus().setCurrentTabId(firstTab.getId());
        }


        List<String> views = new ArrayList<String>();
        if (getCurrentTab().getViews() != null) {
            for (ViewConfig view : getCurrentTab().getViews()) {
                views.add(view.getTitle());
            }
        }


        if (getStatus().getCurrentView() == null && views.size() > 0) {
            getStatus().setCurrentView(views.get(0));
        }

        // Check that the current view is visible
        List<ViewConfig> filteredViews = new ArrayList<ViewConfig>();
        VisiblePredicate predicate = new VisiblePredicate(getStatus().getORI().getParent(), getStatus().getEntitySelections());
        CollectionUtils.select(getCurrentTab().getViews(), predicate, filteredViews);

        boolean visibleView = false;
        String currentView = getStatus().getCurrentView();
        for (ViewConfig view : filteredViews) {
            if (view.getTitle().equals(currentView)) {
                visibleView = true;
                break;
            }
        }

        if (!visibleView) {
            getStatus().setCurrentView(filteredViews.get(0).getTitle());
        }

        ViewConfig viewConfig = null;
        if (getCurrentTab().getViews() != null) {
            for (ViewConfig view : getCurrentTab().getViews()) {
                if (view.getTitle().equals(getStatus().getCurrentView())) {
                    viewConfig = view;
                    break;
                }
            }
        }

        if (viewConfig == null) {
            addOrReplace(new EmptyPanel("content"));
        } else {
            if (viewConfig.getLeft() != null && viewConfig.getTop() != null) {
                addOrReplace(new TopleftLayout("content", viewConfig, getModel()));
            } else if (viewConfig.getLeft() != null) {
                addOrReplace(new LeftMainLayout("content", viewConfig, getModel()));
            } else if (viewConfig.getTop() != null || viewConfig.getTopRight() != null) {
                addOrReplace(new TopmainLayout("content", viewConfig, getModel()));
            } else {
                addOrReplace(new SingleLayout("content", viewConfig, getModel()));
            }
        }

        boolean visible = !isEmbed();
        get("selection").setVisible(visible);
        get("tabs").setVisible(visible);
        //TODO viewSelector.setVisible(visible && views.size() > 1);

        super.onBeforeRender();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        if (isEmbed()) {
            response.render(CssHeaderItem.forCSS("div.tab-row { display: none; }", "embed-browser-page"));
        }
    }

    private class VisibleTabs extends AbstractReadOnlyModel<List<TabGroup>> {

        @Override
        public List<TabGroup> getObject() {

            List<TabConfig> allTabs = getConfig().getTabs();

            // A predicate that filters the visible views
            Predicate filter = new VisiblePredicate(getStatus().getORI().getParent(), getStatus().getEntitySelections());

            // Return a new collection with only the visible tabs
            List<TabConfig> tabs = new ArrayList<TabConfig>();
            CollectionUtils.select(allTabs, filter, tabs);

            // Tab groups
            List<TabGroup> tabGroups = new ArrayList<TabGroup>();
            Map<String, TabGroup> subMenus = new HashMap<String, TabGroup>();

            for (TabConfig tab : tabs) {
                if (tab.getGroup() == null) {
                    tabGroups.add(new TabGroup(tab));
                } else {
                    TabGroup group;
                    if (!subMenus.containsKey(tab.getGroup())) {
                        group = new TabGroup(tab.getGroup());
                        subMenus.put(tab.getGroup(), group);
                        tabGroups.add(group);
                    } else {
                        group = subMenus.get(tab.getGroup());
                    }
                    group.add(tab);
                }
            }

            return tabGroups;
        }

    }

    private static class TabGroup implements Serializable {

        private String groupLabel;
        private List<TabConfig> tabConfigs = new ArrayList<TabConfig>();

        public TabGroup(String groupLabel) {
            super();
            this.groupLabel = groupLabel;
        }

        public TabGroup(TabConfig tab) {
            super();
            this.groupLabel = null;
            this.tabConfigs.add(tab);
        }

        public boolean hasSubMenu() {
            return groupLabel != null;
        }

        public void add(TabConfig tabConfig) {
            tabConfigs.add(tabConfig);
        }

        public boolean containsTab(String tabId) {
            for (TabConfig tab : tabConfigs) {
                if (tab.getId().equals(tabId)) {
                    return true;
                }
            }
            return false;
        }

        public String getGroupLabel() {
            return groupLabel;
        }

        public List<TabConfig> getTabConfigs() {
            return tabConfigs;
        }
    }


}
