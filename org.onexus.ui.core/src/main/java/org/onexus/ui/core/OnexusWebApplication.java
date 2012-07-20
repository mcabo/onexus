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
package org.onexus.ui.core;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.util.resource.ClassPathResourceFinder;
import org.apache.wicket.core.util.resource.locator.OsgiResourceStreamLocator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.resource.ResourceReference;
import org.onexus.ui.core.pages.resource.ResourcesPage;
import org.onexus.ui.core.ws.WebserviceResource;
import org.wicketstuff.osgi.inject.OsgiComponentInjector;
import org.wicketstuff.osgi.inject.impl.OsgiServiceProxyTargetLocator;

import javax.inject.Inject;

public class OnexusWebApplication extends AuthenticatedWebApplication {

    // Force to import the package
    public final static WicketFilter wicketFilter = null;
    public final static OsgiServiceProxyTargetLocator targetLocator = null;

    private OsgiComponentInjector injector;

    public Class<? extends Page> getHomePage() {
        return ResourcesPage.class;
    }

    @Override
    protected void init() {
        super.init();

        // Debug mode
        getDebugSettings().setAjaxDebugModeEnabled(false);
        getMarkupSettings().setStripWicketTags(true);

        // Webservices
        getSharedResources().add("webservice", new WebserviceResource());
        getSharedResources().add("dataservice", new WebserviceResource());
        mountResource("/onx", getSharedResources().get(Application.class, "webservice", null, null, null, true));
        mountResource("/data/${data}", getSharedResources().get(Application.class, "dataservice", null, null, null, true));

        // Authentication
        getApplicationSettings().setAccessDeniedPage(getSignInPageClass());

        // Injection
        getComponentInstantiationListeners().add(getInjector());

        // OSGi
        getResourceSettings().setResourceStreamLocator(new OsgiResourceStreamLocator());

        // Mount pages
        mountPage("/login", getSignInPageClass());
        mountPage("admin", ResourcesPage.class);

    }

    public OsgiComponentInjector getInjector() {

        if (injector == null) {
            injector = new OsgiComponentInjector();
        }

        return injector;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return OnexusSignInPage.class;
    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return OnexusWebSession.class;
    }

    public static OnexusWebApplication get() {
        return (OnexusWebApplication) Application.get();
    }

    public static void inject(Object object) {

        OnexusWebApplication app = get();

        if (app != null) {
            OsgiComponentInjector injector = app.getInjector();

            if (injector != null) {
                injector.inject(object);
            }
        }

    }

    public ResourceReference getWebService() {
        return getSharedResources().get(Application.class, "webservice", null, null, null, true);
    }

    public ResourceReference getDataService() {
        return getSharedResources().get(Application.class, "dataservice", null, null, null, true);
    }

}