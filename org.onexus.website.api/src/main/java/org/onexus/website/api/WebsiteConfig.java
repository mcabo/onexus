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

import org.apache.commons.lang3.SerializationUtils;
import org.onexus.resource.api.annotations.ResourceAlias;
import org.onexus.resource.api.annotations.ResourceImplicitList;
import org.onexus.website.api.utils.authorization.IAuthorization;
import org.onexus.website.api.widget.WidgetConfig;
import org.onexus.website.api.widget.WidgetStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@ResourceAlias("website")
public class WebsiteConfig extends WidgetConfig implements IAuthorization {

    private WebsiteStatus defaultStatus;

    private String authorization;

    private Boolean login;

    private String signInPage;

    @NotNull @Valid
    private List<WidgetConfig> pages;

    private String header;

    private String css;

    private String markup;

    private String bottom;

    @Valid
    @ResourceImplicitList("connection")
    private List<Connection> connections;

    public WebsiteConfig() {
        super();
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    @Override
    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public Boolean getLogin() {
        return login;
    }

    public void setLogin(Boolean login) {
        this.login = login;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public String getMarkup() {
        return markup;
    }

    public void setMarkup(String markup) {
        this.markup = markup;
    }

    public String getBottom() {
        return bottom;
    }

    public void setBottom(String bottom) {
        this.bottom = bottom;
    }

    public String getSignInPage() {
        return signInPage;
    }

    public void setSignInPage(String signInPage) {
        this.signInPage = signInPage;
    }

    public List<WidgetConfig> getPages() {
        return pages;
    }

    public WidgetConfig getPage(String pageId) {
        if (pages != null) {
            for (WidgetConfig page : pages) {
                if (pageId.equals(page.getId())) {
                    return page;
                }
            }
        }

        return null;
    }

    public void setPages(List<WidgetConfig> pages) {
        this.pages = pages;
    }

    public WebsiteStatus getDefaultStatus() {
        return defaultStatus;
    }

    public WebsiteStatus createEmptyStatus() {
        return new WebsiteStatus();
    }

    public void setDefault(WebsiteStatus defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public WebsiteStatus newStatus() {

        WebsiteStatus status = getDefaultStatus();

        if (status != null) {
            status = SerializationUtils.clone(status);
        } else {
            status = createEmptyStatus();
        }

        status.setConfig(this);

        // Add page status
        if (getPages() != null) {
            List<WidgetStatus> pageStatuses = new ArrayList<WidgetStatus>();
            for (WidgetConfig page : getPages()) {
                page.setWebsiteConfig(this);
                pageStatuses.add(page.newStatus());
            }
            status.setChildren(pageStatuses);
        }

        return status;
    }

}
