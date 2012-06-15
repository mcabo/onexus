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
package org.onexus.resource.manager.internal;

import org.onexus.core.IResourceManager;
import org.onexus.core.IResourceSerializer;
import org.onexus.core.resources.*;
import org.onexus.core.utils.ResourceUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

public class ResourceManager implements IResourceManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceManager.class);

    // Injected OSGi services
    private IResourceSerializer serializer;
    private BundleContext context;

    // Loaders and managers
    private PluginLoader pluginLoader;
    private ProjectsContainer projectsContainer;
    private Map<String, ProjectManager> projectManagers;

    public ResourceManager() {
        super();
    }

    public void init() {

        // Projects container
        this.projectsContainer = new ProjectsContainer();

        // Load projects managers
        this.projectManagers = new HashMap<String, ProjectManager>();
        for (String projectUri : this.projectsContainer.getProjectUris()) {
            File projectFolder = projectsContainer.getProjectFolder(projectUri);
            this.projectManagers.put(projectUri, new ProjectManager(serializer, projectUri, projectFolder));
        }

        // Load plugins
        this.pluginLoader = new PluginLoader(context);
        for (Project project : getProjects()) {
            if (project.getPlugins() != null) {
                for (Plugin plugin : project.getPlugins()) {
                     this.pluginLoader.load(plugin);
                }
            }
        }

    }

    @Override
    public Project getProject(String projectUri) {

        ProjectManager projectManager = getProjectManager(projectUri);

        if (projectManager == null) {
            return null;
        }

        return projectManager.getProject();
    }

    @Override
    public List<Project> getProjects() {

        List<Project> projects = new ArrayList<Project>();

        for (ProjectManager projectManager : projectManagers.values()) {
            projects.add(projectManager.getProject());
        }

        return projects;
    }

    @Override
    public <T extends Resource> T load(Class<T> resourceType, String resourceURI) {
        assert resourceType != null;

        if (resourceURI == null) {
            return null;
        }

        resourceURI = ResourceUtils.normalizeUri(resourceURI);

        ProjectManager projectManager = getProjectManager(resourceURI);

        T output = (T) projectManager.getResource(resourceURI);

        return output;
    }

    @Override
    public <T extends Resource> List<T> loadChildren(Class<T> resourceType, String parentURI) {
        assert resourceType != null;
        assert parentURI != null;

        ProjectManager projectManager = getProjectManager(parentURI);

        return projectManager.getResourceChildren(resourceType, parentURI);
    }

    @Override
    public void save(Resource resource) {

        if (resource == null) {
            return;
        }

        ProjectManager projectManager = getProjectManager(resource.getURI());
        projectManager.save(resource);
    }

    @Override
    public void remove(String resourceUri) {

        if (resourceUri == null) {
            return;
        }

        ProjectManager projectManager = getProjectManager(resourceUri);
        projectManager.remove(resourceUri);

    }

    @Override
    public void importProject(String projectUri) {
        File projectFolder = projectsContainer.projectImport(projectUri);
        this.projectManagers.put(projectUri, new ProjectManager(serializer, projectUri, projectFolder));
    }

    @Override
    public void syncProject(String projectURI) {
        ProjectManager projectManager = getProjectManager(projectURI);
        projectManager.loadResources();
    }

    private ProjectManager getProjectManager(String resourceUri) {

        String projectUri = ResourceUtils.getProjectURI(resourceUri);
        ProjectManager projectManager = projectManagers.get(projectUri);

        if (projectManager == null) {
            throw new InvalidParameterException("Project '" + projectUri + "' is not imported");
        }

        return projectManager;

    }

    public IResourceSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(IResourceSerializer serializer) {
        this.serializer = serializer;
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }


}
