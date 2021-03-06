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
package org.onexus.resource.manager.internal.ws.git;

import org.eclipse.jgit.util.Base64;
import org.eclipse.jgit.util.StringUtils;
import org.onexus.resource.api.*;
import org.onexus.resource.api.session.LoginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.text.MessageFormat;

public class GitSecurityFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitSecurityFilter.class);

    private static final String BASIC = "Basic";
    private static final String CHALLENGE = BASIC + " realm=\"karaf\"";

    private static final String gitReceivePack = "/git-receive-pack";
    private static final String gitUploadPack = "/git-upload-pack";
    private static final String[] suffixes = {gitReceivePack, gitUploadPack, "/info/refs", "/HEAD", "/objects"};

    private IAuthorizationManager authorizationManager;
    private IResourceManager resourceManager;
    private IProfileManager profileManager;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.debug("FilterConfig: " + filterConfig);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String fullUrl = getFullUrl(httpRequest);
        String repository = extractRepositoryName(fullUrl);
        String fullSuffix = fullUrl.substring(repository.length());
        String requestPrivilege = getUrlRequestAction(fullSuffix);
        String user = getUser(httpRequest);

        if (IAuthorizationManager.WRITE.equals(requestPrivilege) && user == null) {
            httpResponse.setHeader("WWW-Authenticate", CHALLENGE);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Find a project with this name
        ORI resource = null;
        for (Project project : resourceManager.getProjects()) {
            if (repository.equals(project.getName())) {
                resource = project.getORI();
                break;
            }
        }

        if (resource == null) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!authorizationManager.check(requestPrivilege, resource) && user == null) {
            httpResponse.setHeader("WWW-Authenticate", CHALLENGE);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Project project = null;
        try {
            project = resourceManager.getProject(resource.getProjectUrl());
        } catch (Exception e) {
            LOGGER.error("At git serving '" + resource + "'", e);
        }

        //TODO automatic project creation on push
        // if (project == null && requestPrivilege.equals(IAuthorizationManager.WRITE)) {
        //
        // }

        if (project == null) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        chain.doFilter(new AuthenticatedRequest(user, httpRequest), httpResponse);

    }

    protected String getUser(HttpServletRequest httpRequest) {

        final String authorization = httpRequest.getHeader("Authorization");

        if (authorization != null && authorization.startsWith(BASIC)) {

            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring(BASIC.length()).trim();
            String credentials = new String(Base64.decode(base64Credentials), Charset.forName("UTF-8"));

            // credentials = username:password
            final String[] values = credentials.split(":", 2);

            if (values.length == 2) {
                String username = values[0];
                String password = values[1];

                LoginContext.set(new LoginContext(username), httpRequest.getRequestedSessionId());

                boolean valid = false;
                String[] tokens = profileManager.getValueArray("tokens");
                if (tokens != null) {
                    for (String token : tokens) {
                        if (password.equals(token)) {
                            valid = true;
                            break;
                        }
                    }
                }

                if (valid) {
                    return username;
                }

                LoginContext.get().logout();
            }
            LOGGER.info(MessageFormat.format("AUTH: invalid credentials ({0})", credentials));
        }
        return null;
    }

    @Override
    public void destroy() {
    }

    private String getFullUrl(HttpServletRequest httpRequest) {
        String servletUrl = httpRequest.getContextPath() + httpRequest.getServletPath();
        String url = httpRequest.getRequestURI().substring(servletUrl.length());
        String params = httpRequest.getQueryString();
        if (url.length() > 0 && url.charAt(0) == '/') {
            url = url.substring(1);
        }

        return url + (StringUtils.isEmptyOrNull(params) ? "" : "?" + params);
    }

    private String extractRepositoryName(String url) {
        String repository = url;
        // get the repository name from the url by finding a known url suffix
        for (String urlSuffix : suffixes) {
            if (repository.indexOf(urlSuffix) > -1) {
                repository = repository.substring(0, repository.indexOf(urlSuffix));
            }
        }
        return repository;
    }

    private String getUrlRequestAction(String suffix) {
        if (!StringUtils.isEmptyOrNull(suffix)) {
            if (suffix.startsWith(gitReceivePack)) {
                return IAuthorizationManager.WRITE;
            } else if (suffix.startsWith(gitUploadPack)) {
                return IAuthorizationManager.READ;
            } else if (suffix.contains("?service=git-receive-pack")) {
                return IAuthorizationManager.WRITE;
            } else if (suffix.contains("?service=git-upload-pack")) {
                return IAuthorizationManager.READ;
            } else {
                return IAuthorizationManager.READ;
            }
        }
        return null;
    }

    public IAuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    public void setAuthorizationManager(IAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public IResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public IProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(IProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    /**
     * Wraps a standard HttpServletRequest and overrides user principal methods.
     */
    public static class AuthenticatedRequest extends ServletRequestWrapper {

        private String user;

        public AuthenticatedRequest(String user, HttpServletRequest req) {
            super(req);
            this.user = user;
        }

        @Override
        public String getRemoteUser() {
            return user;
        }

        @Override
        public Principal getUserPrincipal() {
            return new Principal() {
                @Override
                public String getName() {
                    return user;
                }
            };
        }
    }
}
