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
package org.onexus.website.api.utils.panels;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

public class SignInPage extends WebPage {
    public static final CssResourceReference CSS = new CssResourceReference(SignInPage.class, "SignInPage.css");
    private static final long serialVersionUID = 1L;

    /**
     * Construct
     */
    public SignInPage() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param parameters The page parameters
     */
    public SignInPage(final PageParameters parameters) {
        SignInPanel signPanel = null;
        add(signPanel = new SignInPanel("signInPanel"));
        signPanel.setRememberMe(false);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(CSS));
    }
}