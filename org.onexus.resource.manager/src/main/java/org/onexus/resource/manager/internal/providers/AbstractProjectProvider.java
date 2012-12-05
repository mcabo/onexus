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
package org.onexus.resource.manager.internal.providers;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.onexus.data.api.Data;
import org.onexus.resource.api.*;
import org.onexus.resource.api.exceptions.UnserializeException;
import org.onexus.resource.manager.internal.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

public abstract class AbstractProjectProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractProjectProvider.class);
    public final static String ONEXUS_EXTENSION = "onx";
    public final static String ONEXUS_PROJECT_FILE = "onexus-project." + ONEXUS_EXTENSION;

    private IResourceSerializer serializer;

    private PluginLoader pluginLoader;
	private Set<Long> bundleDependencies = new HashSet<Long>();

    private String projectName;
    private String projectUrl;
    private File projectFolder;


	private FileAlterationObserver observer;

    private Project project;
    private Map<ORI, Resource> resources;

	private List<IResourceListener> listeners = new ArrayList<IResourceListener>();

	public AbstractProjectProvider(String projectName, String projectUrl, File projectFolder, FileAlterationMonitor monitor, List<IResourceListener> listeners) {
        super();
        this.projectName = projectName;
        this.projectUrl = projectUrl;
        this.projectFolder = projectFolder;
		this.listeners = listeners;


		// Don't watch hidden folders and files
		IOFileFilter notHiddenDirectoryFilter = FileFilterUtils.notFileFilter(
				FileFilterUtils.or(
					FileFilterUtils.and(
							FileFilterUtils.directoryFileFilter(),
							HiddenFileFilter.HIDDEN
					),
					HiddenFileFilter.HIDDEN
				)
		);

		observer = new FileAlterationObserver(projectFolder, notHiddenDirectoryFilter);
		observer.addListener(new FileAlterationListenerAdaptor() {

			@Override
			public void onDirectoryCreate(File directory) {
				log.info("Creating folder '" + directory.getName() + "'");
				onResourceCreate( loadFile(directory) );
			}

			@Override
			public void onDirectoryDelete(File directory) {
				log.info("Deleting folder '" + directory.getName() + "'");
				ORI resourceOri = convertFileToORI(directory);
				onResourceDelete( resources.remove(resourceOri) );
			}

			@Override
			public void onFileChange(File file) {
				log.info("Reloading file '" + file.getName() + "'");
				onResourceChange( loadFile(file) );
			}

			@Override
			public void onFileCreate(File file) {
				log.info("Creating file '" + file.getName() + "'");
				onResourceCreate( loadFile(file) );
			}

			@Override
			public void onFileDelete(File file) {
				log.info("Deleting file '" + file.getName() + "'");
				ORI resourceOri = convertFileToORI(file);
				onResourceDelete( resources.remove(resourceOri) );
			}

			private Resource loadFile(File file) {
				// Skip project file
				if (ONEXUS_PROJECT_FILE.equals(file.getName())) {
					return null;
				}

				Resource resource = loadResource(file);

				if (resource != null) {
					resources.put(resource.getURI(), resource);
				}

				return resource;
			}
		});

		monitor.addObserver(observer);

    }

    public Project getProject() {
        if (project == null) {
            loadProject();
        }

        return project;
    }

    public File getProjectFolder() {
        return projectFolder;
    }

    protected void setProjectFolder(File projectFolder) {
        this.projectFolder = projectFolder;
    }

    public void loadProject() {
        File projectOnx = new File(projectFolder, ONEXUS_PROJECT_FILE);

        if (!projectOnx.exists()) {
            throw new InvalidParameterException("No Onexus project in " + projectFolder.getAbsolutePath());
        }

        this.project = (Project) loadResource(projectOnx);
        this.project.setName(projectName);

		this.bundleDependencies.clear();

		if (project.getPlugins() != null) {
			for (Plugin plugin : project.getPlugins()) {
				try {
					long bundleId = pluginLoader.load(plugin);
					bundleDependencies.add(bundleId);
				} catch (InvalidParameterException e) {
					log.error(e.getMessage());
				}
			}
		}

		this.resources = null;
    }

    private void loadResources() {

        this.resources = new HashMap<ORI, Resource>();

        Collection<File> files = addFilesRecursive(new ArrayList<File>(), projectFolder);

        for (File file : files) {

            // Skip project file
            if (ONEXUS_PROJECT_FILE.equals(file.getName())) {
                continue;
            }

            Resource resource = loadResource(file);

            if (resource != null) {

				if (resources == null) {
					return;
				}

                resources.put(resource.getURI(), resource);
            }
        }
    }

    public Resource getResource(ORI resourceUri) {

        if (resourceUri.getPath() == null && projectUrl.equals(resourceUri.getProjectUrl())) {
            return project;
        }

        if (resources == null) {
            loadResources();
        }

        if (!resources.containsKey(resourceUri)) {
            throw new UnsupportedOperationException("Resource '" + resourceUri + "' is not defined in any project.");
        }

        return resources.get(resourceUri);

    }

    public <T extends Resource> List<T> getResourceChildren(IAuthorizationManager authorizationManager, Class<T> resourceType, ORI parentURI) {

        if (resources == null) {
            loadResources();
        }

        List<T> children = new ArrayList<T>();
        for (Resource resource : resources.values()) {
            if (parentURI.isChild(resource.getURI()) && resourceType.isAssignableFrom(resource.getClass())) {
                if (authorizationManager.check(IAuthorizationManager.READ, resource.getURI())) {
                    children.add((T) resource);
                }
            }
        }

        return children;
    }

    public void syncProject() {
        loadProject();
        loadResources();
    }

    public void updateProject() {
        importProject();
        syncProject();
    }

    protected abstract void importProject();

    private Resource loadResource(File resourceFile) {

        String resourceName;
        Resource resource;

        if (ONEXUS_EXTENSION.equalsIgnoreCase(FilenameUtils.getExtension(resourceFile.getName()))) {

            resourceName = FilenameUtils.getBaseName(resourceFile.getName());

            try {

                resource = serializer.unserialize(Resource.class, new FileInputStream(resourceFile));

            } catch (FileNotFoundException e) {
                resource = createErrorResource(resourceFile, "File '" + resourceFile.getPath() + "' not found.");
            } catch (UnserializeException e) {
                resource = createErrorResource(resourceFile, "Parsing file " + resourceFile.getPath() + " at line " + e.getLine() + " on " + e.getPath());
            } catch (Exception e) {
                resource = createErrorResource(resourceFile, e.getMessage());
            }

        } else {

            resourceName = resourceFile.getName();

            if (resourceFile.isDirectory()) {
                resource = new Folder();
            } else {
                resource = createDataResource(resourceFile);
            }

        }

        if (resource == null) {
            return null;
        }

        resource.setURI(convertFileToORI(resourceFile));
        return resource;

    }

	private ORI convertFileToORI(File file) {

		String projectPath = projectFolder.getAbsolutePath() + File.separator;
		String filePath = file.getAbsolutePath();
		String relativePath = filePath.replace(projectPath, "");

		if (relativePath.equals(ONEXUS_PROJECT_FILE)) {
			relativePath = null;
		} else {
			relativePath = relativePath.replace("." + ONEXUS_EXTENSION, "");
		}

		return new ORI(projectUrl, relativePath);

	}

    private Resource createErrorResource(File resourceFile, String msg) {
        log.error(msg);
        Resource errorResource = createDataResource(resourceFile);
        errorResource.setDescription("ERROR: " + msg);
        return errorResource;
    }

    private Resource createDataResource(File resourceFile) {
        Data data = new Data();
        Loader loader = new Loader();
        loader.setParameters(new ArrayList<Parameter>());
        loader.getParameters().add(new Parameter("data-url", resourceFile.toURI().toString()));
        data.setLoader(loader);
        return data;
    }

    private static Collection<File> addFilesRecursive(Collection<File> files, File parentFolder) {

        if (parentFolder.isDirectory()) {
            File[] inFiles = parentFolder.listFiles();
            if (inFiles != null) {
                for (File file : inFiles) {
                    if (!file.isHidden()) {
                        files.add(file);
                        if (file.isDirectory()) {
                            addFilesRecursive(files, file);
                        }
                    }
                }
            }
        }

        return files;
    }

    public void save(Resource resource) {

        if (resource == null) {
            return;
        }

        if (resource instanceof Project) {
            throw new IllegalArgumentException("Cannot create a project '" + resource.getURI() + "' inside project '" + projectUrl + "'");
        }

        String resourcePath = resource.getURI().getPath();

        File file;
        if (resource instanceof Folder) {
            file = new File(projectFolder, resourcePath);
            file.mkdirs();

            return;
        }


        file = new File(projectFolder, resourcePath + "." + ONEXUS_EXTENSION);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream os = new FileOutputStream(file);
            serializer.serialize(resource, os);
            os.close();

        } catch (IOException e) {
            log.error("Saving resource '" + resource.getURI() + "' in file '" + file.getAbsolutePath() + "'", e);
        }

		observer.checkAndNotify();

    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    public void setPluginLoader(PluginLoader pluginLoader) {
        this.pluginLoader = pluginLoader;
    }

    public IResourceSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(IResourceSerializer serializer) {
        this.serializer = serializer;
    }

	protected void onResourceCreate(Resource resource) {

		if (resource == null) {
			return;
		}

		log.info("Resource '" + resource.getName() + "' created.");

		for (IResourceListener listener : listeners) {
			listener.onResourceCreate(resource);
		}
	}

	protected void onResourceChange(Resource resource) {

		if (resource == null) {
			return;
		}

		log.info("Resource '" + resource.getName() + "' changed.");

		for (IResourceListener listener : listeners) {
			listener.onResourceChange(resource);
		}
	}

	protected void onResourceDelete(Resource resource) {

		if (resource == null) {
			return;
		}

		log.info("Resource '" + resource.getName() + "' deleted.");

		for (IResourceListener listener : listeners) {
			listener.onResourceDelete(resource);
		}
	}

	public boolean dependsOnBundle(long bundleId) {
		return bundleDependencies.contains(Long.valueOf(bundleId));
	}
}