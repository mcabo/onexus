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
package org.onexus.ui.workspace.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.AbstractWrapModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.h2.util.StringUtils;
import org.onexus.core.IResourceManager;
import org.onexus.core.resources.Project;
import org.onexus.core.resources.Resource;
import org.onexus.core.utils.ResourceUtils;
import org.onexus.ui.IViewerCreator;
import org.onexus.ui.viewers.EmptyPanelViewerCreator;
import org.onexus.ui.viewers.IViewersManager;
import org.onexus.ui.workspace.events.AjaxUpdateOnEvent;
import org.onexus.ui.workspace.events.EventResourceSelect;
import org.onexus.ui.viewers.tree.ProjectTree;
import org.onexus.ui.viewers.tree.ResourceTreeProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ResourcesPage extends BasePage {

    public final static String RESOURCE = "uri";

    @Inject
    public IResourceManager resourceManager;

    @Inject
    public IViewersManager viewersManager;

    public ResourcesPage(PageParameters parameters) {
        super(parameters);

        // Current selected resource model
        final IModel<Resource> resourceModel = new CurrentResourceModel(parameters);

        // Reload project
        add(new ToolProjectReload("projectReload"));

        // Copy resource URI
        add(new ToolCopyUri("copyURI", resourceModel));

        // Resource URI breadcrumb
        add(new ToolUriBreadCrumb("uriBreadCrumb", resourceModel));

        // Tabs
        add(new ViewerTabs("tabs", resourceModel ));

    }

    public class ToolProjectReload extends Link<Resource> {

        public ToolProjectReload(String id) {
            super(id);
        }

        @Override
        public void onClick() {
            resourceManager.checkout();
        }
    }

    public static class ToolCopyUri extends ExternalLink {

        public ToolCopyUri(String id, IModel<Resource> resourceModel) {
            super(id, new PropertyModel<String>(resourceModel, "URI"));

            add(new AjaxUpdateOnEvent(EventResourceSelect.EVENT));
        }
    }

    public static class ToolUriBreadCrumb extends WebMarkupContainer {


        public ToolUriBreadCrumb(String id, IModel<Resource> resource) {
            super(id);

            add(new AjaxUpdateOnEvent(EventResourceSelect.EVENT));

            // Add list items
            add(new ListView<String>("uri", new BreadCrumbItemsModel(resource)) {

                private transient List<String> links;

                @Override
                protected void onBeforeRender() {

                    BreadCrumbItemsModel model = (BreadCrumbItemsModel) getModel();

                    List<String> uriItems = model.getObject();

                    if (uriItems == null) {
                        links = Collections.EMPTY_LIST;
                    }

                    links = new ArrayList<String>(uriItems.size());

                    int positionOnxItem = -1;


                    Resource resource = model.getWrappedModel().getObject();
                    String serverUri = (resource == null ? null : ResourceUtils.getServerURI(resource.getURI()));

                    for (int i = 0; i < uriItems.size(); i++) {
                        String uriItem = uriItems.get(i);

                        if (uriItem.equalsIgnoreCase("ONX")) {
                            positionOnxItem = i;
                            links.add(null);
                            continue;
                        }

                        if (positionOnxItem < 0) {
                            links.add(null);
                            continue;
                        }

                        StringBuilder link = new StringBuilder();
                        link.append(serverUri);
                        for (int t = positionOnxItem + 1; t <= i; t++) {
                            link.append(Resource.SEPARATOR);
                            link.append(uriItems.get(t));
                        }
                        links.add(link.toString());

                    }

                    super.onBeforeRender();
                }

                @Override
                protected void populateItem(ListItem<String> item) {

                    // Link
                    PageParameters parameters = new PageParameters();
                    String resourceUri = links.get(item.getIndex());
                    parameters.set(ResourcesPage.RESOURCE, resourceUri);
                    Link<String> link = new BookmarkablePageLink<String>("link", ResourcesPage.class, parameters);
                    link.setEnabled(resourceUri != null);

                    // Label
                    link.add(new Label("name", item.getModelObject()));

                    // Divider
                    boolean showDivider = !(getModelObject().size() - 1 == item.getIndex());
                    item.add(new WebMarkupContainer("divider").setVisible(showDivider));
                    item.add(link);

                }
            });

        }

    }

    public class ViewerTabs extends WebMarkupContainer {


        public ViewerTabs(String id, final IModel<Resource> resourceModel ) {
            super(id);

            final IModel<Integer> currentViewer = new Model<Integer>(0);
            final ViewerCreatorsModel viewerCreators = new ViewerCreatorsModel(resourceModel);

            add(new AjaxUpdateOnEvent(EventResourceSelect.EVENT));

            add(new ListView<IViewerCreator>("tab", viewerCreators) {

                @Override
                protected void populateItem(ListItem<IViewerCreator> item) {

                    AjaxLink<Integer> link = new AjaxLink<Integer>("link", Model.of(item.getIndex()) ) {

                        @Override
                        public void onClick(AjaxRequestTarget target) {

                               // Update selected tab
                               Integer pos = getModelObject();
                               currentViewer.setObject(pos);

                               // Update main panel
                               IViewerCreator viewerCreator = viewerCreators.getViewerCreatorAt(pos);
                               ResourcesPage.this.addOrReplace(viewerCreators.getViewerCreatorAt(pos).getPanel("main", resourceModel).setOutputMarkupId(true));

                               // Refresh using AJAX
                               target.add(ResourcesPage.this.get("main"));
                               target.add(ViewerTabs.this);
                        }
                    };

                    link.add(new Label("label", item.getModelObject().getTitle()));
                    item.add(link);

                    if (item.getIndex() == currentViewer.getObject()) {
                        item.add(new AttributeAppender("class", "active"));
                    }
                }
            });

            ResourcesPage.this.add(viewerCreators.getViewerCreatorAt(0).getPanel("main", resourceModel).setOutputMarkupId(true));


        }



    }

    private class ViewerCreatorsModel extends AbstractWrapModel<List<IViewerCreator>> {

        private IModel<Resource> resourceModel;

        private ViewerCreatorsModel(IModel<Resource> resourceModel) {
            this.resourceModel = resourceModel;
        }

        @Override
        public List<IViewerCreator> getObject() {

            Resource resource = resourceModel.getObject();

            if (resource == null) {
                return Collections.EMPTY_LIST;
            }

            return viewersManager.getViewerCreators(resource);
        }

        @Override
        public IModel<?> getWrappedModel() {
            return resourceModel;
        }

        public IViewerCreator getViewerCreatorAt(int pos) {
            List<IViewerCreator> viewerCreators = getObject();

            if (viewerCreators == null || viewerCreators.isEmpty()) {
                return new EmptyPanelViewerCreator();
            }

            if (viewerCreators.size() <= pos) {
                return viewerCreators.get(0);
            }

            return viewerCreators.get(pos);
        }
    }

    private static class BreadCrumbItemsModel extends AbstractWrapModel<List<String>> {

        private IModel<Resource> resource;
        private transient List<String> object;

        private BreadCrumbItemsModel(IModel<Resource> resource) {
            super();
            this.resource = resource;
        }

        @Override
        public List<String> getObject() {

            if (object == null) {

                Resource resource = this.resource.getObject();

                if (resource == null || StringUtils.isNullOrEmpty(resource.getURI())) {
                    return Collections.EMPTY_LIST;
                }

                String uri = resource.getURI();

                // Remove protocol section
                int protocol = uri.indexOf(":/");
                uri = (protocol > 0) ? uri.substring(protocol + 2) : uri;

                // Remove separators at first positon
                if (uri.charAt(0) == '/') {
                    uri = uri.substring(1);
                }

                if (uri.charAt(uri.length() - 1) == '/') {
                    uri = uri.substring(0, uri.length() - 2);
                }

                object = Arrays.asList(uri.split(Pattern.quote("/")));
            }

            return object;
        }

        @Override
        public void detach() {
            this.object = null;
        }

        @Override
        public IModel<Resource> getWrappedModel() {
            return resource;
        }
    }



    private class CurrentResourceModel implements IModel<Resource> {

        private String selectedResource;

        public CurrentResourceModel(PageParameters parameters) {
            super();

            selectedResource = parameters.get(RESOURCE).toOptionalString();

            if (selectedResource == null) {
                selectedResource = getDefaultProjectURI();
            }
        }

        @Override
        public Resource getObject() {
            return resourceManager.load(Resource.class, selectedResource);
        }

        @Override
        public void setObject(Resource object) {
            selectedResource = object.getURI();
        }

        @Override
        public void detach() {
        }

        @Override
        public int hashCode() {
            return selectedResource.hashCode();
        }

        @Override
        public boolean equals(Object obj) {

            if (obj instanceof CurrentResourceModel) {
                Resource resource = ((CurrentResourceModel) obj).getObject();
                if (resource != null) {
                    return StringUtils.equals(selectedResource, resource.getURI());
                }
            }

            return false;
        }

        private String getDefaultProjectURI() {
            List<Project> projects = resourceManager.loadChildren(Project.class, null);

            if (projects != null && !projects.isEmpty()) {
                return projects.get(0).getURI();
            }

            return null;
        }
    }

}
