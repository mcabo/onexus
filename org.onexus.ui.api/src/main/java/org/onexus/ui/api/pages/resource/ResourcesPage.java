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
package org.onexus.ui.api.pages.resource;

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
import org.apache.wicket.model.AbstractWrapModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.onexus.resource.api.ORI;
import org.onexus.resource.api.Resource;
import org.onexus.ui.api.events.AjaxUpdateOnEvent;
import org.onexus.ui.api.events.EventResourceSelect;
import org.onexus.ui.api.viewers.EmptyPanelViewerCreator;
import org.onexus.ui.api.viewers.IViewerCreator;
import org.onexus.ui.api.viewers.IViewersManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ResourcesPage extends BaseResourcePage {

    public static final String PARAMETER_RESOURCE = "ori";

    @Inject
    private IViewersManager viewersManager;

    public ResourcesPage(PageParameters parameters) {
        super(new ResourceModel(parameters.get(PARAMETER_RESOURCE).toOptionalString()));

        // Sync project
        add(new ToolProjectSync("projectSync"));

        // Update project
        add(new ToolProjectUpdate("projectUpdate"));

        // Copy resource URI
        add(new ToolCopyUri("copyURI", getModel()));

        // Resource URI breadcrumb
        add(new ToolOriBreadCrumb("oriBreadCrumb", getModel()));

        // Tabs
        add(new ViewerTabs("tabs", getModel()));

    }

    public class ToolProjectSync extends Link<Resource> {

        public ToolProjectSync(String id) {
            super(id);
        }

        @Override
        public void onClick() {
            Resource resource = ResourcesPage.this.getModelObject();

            if (resource != null) {
                ORI resourceUri = resource.getORI();
                getResourceManager().syncProject(resourceUri.getProjectUrl());

                Resource newVersion = getResourceManager().load(Resource.class, resourceUri);
                if (newVersion == null) {
                    resourceUri = resourceUri.getParent();

                    newVersion = getResourceManager().load(Resource.class, resourceUri);
                    if (newVersion == null) {
                        resourceUri = new ORI(resourceUri.getProjectUrl(), null);
                    }
                }

                PageParameters parameters = new PageParameters();
                parameters.set(ResourcesPage.PARAMETER_RESOURCE, resourceUri);

                setResponsePage(ResourcesPage.class, parameters);
            }
        }

        @Override
        public boolean isVisible() {
            return ResourcesPage.this.getModelObject() != null;
        }
    }

    public class ToolProjectUpdate extends Link<Resource> {

        public ToolProjectUpdate(String id) {
            super(id);
        }

        @Override
        public void onClick() {
            Resource resource = ResourcesPage.this.getModelObject();

            if (resource != null) {
                ORI resourceUri = resource.getORI();
                getResourceManager().updateProject(resourceUri.getProjectUrl());

                Resource newVersion = getResourceManager().load(Resource.class, resourceUri);
                if (newVersion == null) {
                    resourceUri = resourceUri.getParent();

                    newVersion = getResourceManager().load(Resource.class, resourceUri);
                    if (newVersion == null) {
                        resourceUri = new ORI(resourceUri.getProjectUrl(), null);
                    }
                }

                PageParameters parameters = new PageParameters();
                parameters.set(ResourcesPage.PARAMETER_RESOURCE, resourceUri);

                setResponsePage(ResourcesPage.class, parameters);
            }
        }

        @Override
        public boolean isVisible() {
            return ResourcesPage.this.getModelObject() != null;
        }
    }


    public static class ToolCopyUri extends ExternalLink {

        public ToolCopyUri(String id, IModel<Resource> resourceModel) {
            super(id, new PropertyModel<String>(resourceModel, "ORI"));

            add(new AjaxUpdateOnEvent(EventResourceSelect.EVENT));
        }

        @Override
        public boolean isVisible() {
            return getDefaultModelObject() != null;
        }


    }

    public static class ToolOriBreadCrumb extends WebMarkupContainer {


        public ToolOriBreadCrumb(String id, IModel<Resource> resource) {
            super(id, resource);

            add(new AjaxUpdateOnEvent(EventResourceSelect.EVENT));

            // Add list items
            add(new ListView<String>("ori", new BreadCrumbItemsModel(resource)) {

                private transient List<String> links;

                @Override
                protected void onBeforeRender() {

                    BreadCrumbItemsModel model = (BreadCrumbItemsModel) getModel();

                    List<String> oriItems = model.getObject();

                    if (oriItems == null) {
                        links = Collections.EMPTY_LIST;
                    }

                    links = new ArrayList<String>(oriItems.size());
                    Resource resource = model.getWrappedModel().getObject();
                    String projectUri = resource == null ? null : resource.getORI().getProjectUrl().toString();

                    links.add(projectUri);
                    for (int i = 1; i < oriItems.size(); i++) {
                        StringBuilder link = new StringBuilder();
                        link.append(projectUri).append("?");
                        link.append(oriItems.get(1));
                        for (int t = 2; t <= i; t++) {
                            link.append(ORI.SEPARATOR);
                            link.append(oriItems.get(t));
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
                    parameters.set(ResourcesPage.PARAMETER_RESOURCE, resourceUri);
                    Link<String> link = new BookmarkablePageLink<String>("link", ResourcesPage.class, parameters);
                    link.setEnabled(resourceUri != null);

                    // Label
                    link.add(new Label("name", item.getModelObject()));

                    // Divider
                    boolean showDivider = !(getModelObject().size() - 1 == item.getIndex());
                    Label divider = new Label("divider", item.getIndex() == 0 ? "?" : "/");
                    item.add(divider.setVisible(showDivider));
                    item.add(link);

                }
            });

        }

        @Override
        public boolean isVisible() {
            return getDefaultModelObject() != null;
        }

    }

    public class ViewerTabs extends WebMarkupContainer {


        public ViewerTabs(String id, final IModel<Resource> resourceModel) {
            super(id);

            final IModel<Integer> currentViewer = new Model<Integer>(0);
            final ViewerCreatorsModel viewerCreators = new ViewerCreatorsModel(resourceModel);

            add(new AjaxUpdateOnEvent(EventResourceSelect.EVENT));

            add(new ListView<IViewerCreator>("tab", viewerCreators) {

                @Override
                protected void populateItem(ListItem<IViewerCreator> item) {

                    AjaxLink<Integer> link = new AjaxLink<Integer>("link", Model.of(item.getIndex())) {

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
        private ORI lastResourceUri;
        private transient List<String> object;

        private BreadCrumbItemsModel(IModel<Resource> resource) {
            super();
            this.resource = resource;
        }

        @Override
        public List<String> getObject() {


            if (resource.getObject() == null) {
                return Collections.EMPTY_LIST;
            }

            if (!resource.getObject().getORI().equals(lastResourceUri)) {
                this.object = null;
            }

            if (object == null) {

                Resource resource = this.resource.getObject();
                lastResourceUri = resource.getORI();

                if (resource == null || resource.getORI() == null) {
                    return Collections.EMPTY_LIST;
                }

                ORI ori = resource.getORI();

                object = new ArrayList<String>();

                ORI resourceOri = resource.getORI();
                object.add(resourceOri.getProjectUrl());

                if (!Strings.isEmpty(ori.getPath())) {
                    // Remove the first separator
                    String path = resourceOri.getPath().substring(1);
                    object.addAll(Arrays.asList(path.split(Pattern.quote(String.valueOf(ORI.SEPARATOR)))));
                }

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

}
