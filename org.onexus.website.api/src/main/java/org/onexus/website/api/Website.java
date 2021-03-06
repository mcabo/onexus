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
package org.onexus.website.api;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.StringValue;
import org.onexus.resource.api.ORI;
import org.onexus.resource.api.session.LoginContext;
import org.onexus.website.api.theme.DefaultTheme;
import org.onexus.website.api.utils.CustomCssBehavior;
import org.onexus.website.api.utils.HtmlDataResourceModel;
import org.onexus.website.api.utils.authorization.Authorization;
import org.onexus.website.api.utils.panels.ConnectionsPanel;
import org.onexus.website.api.utils.panels.LoginPanel;
import org.onexus.website.api.utils.panels.NotAuthorizedPage;
import org.onexus.website.api.utils.visible.VisiblePredicate;
import org.onexus.website.api.widget.IWidgetManager;
import org.onexus.website.api.widget.WidgetConfig;
import org.onexus.website.api.widget.WidgetModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Website extends WebPage implements IMarkupResourceStreamProvider {

    // Parameters
    public static final String PARAMETER_CURRENT_PAGE = "c";

    @Inject
    private IWidgetManager pageManager;

    private transient MarkupLoader markupLoader;

    public Website(PageParameters pageParameters) {
        super(new WebsiteModel(pageParameters));

        if (!Authorization.authorize(getConfig())) {
            if (WebsiteSession.get().isSignedIn()) {
                setResponsePage(NotAuthorizedPage.class);
            } else {
                WebsiteApplication.get().restartResponseAtSignInPage();
            }
        }

        LoginContext ctx = LoginContext.get();
        try {

            LoginContext.set(LoginContext.SERVICE_CONTEXT, null);

            add(new DefaultTheme());

            final WebsiteStatus status = getStatus();
            final WebsiteConfig config = getConfig();

            ORI parentUri = config.getORI().getParent();
            add(new CustomCssBehavior(parentUri, config.getCss()));

            // Init currentPage
            VisiblePredicate visiblePredicate = new VisiblePredicate(getConfig().getORI(), Collections.EMPTY_LIST);
            if (status.getCurrentPage() == null ||
                    config.getPage(status.getCurrentPage()) == null ||
                    !visiblePredicate.evaluate(config.getPage(status.getCurrentPage())
                    )) {
                List<WidgetConfig> visiblePages = getPageConfigList();
                if (!visiblePages.isEmpty()) {
                    String currentPage = visiblePages.get(0).getId();
                    status.setCurrentPage(currentPage);
                }
            }

            add(new Label("windowTitle", config.getTitle()));

            add(new EmptyPanel("progressbar"));
            //TODO add(new ProgressBar("progressbar", false));

            String header = config.getHeader();

            Label headerLabel = new Label("header", new HtmlDataResourceModel(parentUri, header));
            headerLabel.setVisible(header != null && !header.isEmpty());
            headerLabel.setEscapeModelStrings(false);
            add(headerLabel);

            WebMarkupContainer menuSection = new WebMarkupContainer("menuSection");
            menuSection.add(new ListView<WidgetConfig>("menu", new PropertyModel<List<WidgetConfig>>(this, "pageConfigList")) {

                @Override
                protected void populateItem(ListItem<WidgetConfig> item) {

                    WidgetConfig pageConfig = item.getModelObject();

                    PageParameters parameters = new PageParameters();
                    parameters.add(PARAMETER_CURRENT_PAGE, pageConfig.getId());

                    Link<String> link = new BookmarkablePageLink<String>("link", Website.class, parameters);
                    link.add(new Label("name", pageConfig.getLabel()));

                    String currentPage = status.getCurrentPage();

                    item.add(link);

                    if (currentPage.equals(pageConfig.getId())) {
                        link.getParent().add(new AttributeModifier("class", "active"));
                    }

                }
            });

            if (pageParameters.get("embed").toBoolean(false)) {
                menuSection.setVisible(false);
            } else {
                menuSection.setVisible(true);
            }

            // Login section
            Panel login = new LoginPanel("login");
            menuSection.add(login);
            if (config.getLogin() == null || !config.getLogin()) {
                login.setVisible(false);
            }

            // Projects section
            menuSection.add(new ConnectionsPanel("connections", config.getConnections()));

            add(menuSection);

            String currentPage = status.getCurrentPage();

            add(pageManager.create("page", new WidgetModel(currentPage, getDefaultModel())));

            String bottom = config.getBottom();
            Label bottomLabel = new Label("bottom", new HtmlDataResourceModel(parentUri, bottom));
            bottomLabel.setVisible(bottom != null && !bottom.isEmpty());
            bottomLabel.setEscapeModelStrings(false);
            add(bottomLabel);

        } finally {
            LoginContext.set(ctx, null);
        }

    }

    @Override
    protected void onBeforeRender() {
        StringValue embed = getPage().getPageParameters().get("embed");

        boolean visible = !embed.toBoolean(false);

        get("header").setVisible(visible);
        get("bottom").setVisible(visible);

        if (!Authorization.authorize(getConfig()) || !Authorization.authorize(getConfig().getPage(getStatus().getCurrentPage()))) {
            if (WebsiteSession.get().isSignedIn()) {
                setResponsePage(NotAuthorizedPage.class);
            } else {
                WebsiteApplication.get().restartResponseAtSignInPage();
            }
        }

        super.onBeforeRender();
    }

    public WebsiteStatus getStatus() {
        return (WebsiteStatus) getDefaultModelObject();
    }

    public WebsiteConfig getConfig() {
        return getStatus().getConfig();
    }

    public List<WidgetConfig> getPageConfigList() {
        List<WidgetConfig> pages = new ArrayList<WidgetConfig>();

        VisiblePredicate visiblePredicate = new VisiblePredicate(getConfig().getORI(), Collections.EMPTY_LIST);

        for (WidgetConfig page : getConfig().getPages()) {

            if (Authorization.authorize(page) && visiblePredicate.evaluate(page)) {
                pages.add(page);
            }
        }

        return pages;
    }

    @Override
    public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {

        if (markupLoader == null) {
            markupLoader = new MarkupLoader(getConfig().getORI(), getConfig().getMarkup());
        }

        return markupLoader.getMarkupResourceStream(container, containerClass);
    }
}
