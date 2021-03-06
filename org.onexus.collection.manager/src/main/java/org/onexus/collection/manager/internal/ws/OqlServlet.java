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
package org.onexus.collection.manager.internal.ws;

import org.onexus.collection.api.*;
import org.onexus.collection.api.query.IQueryParser;
import org.onexus.collection.api.query.Query;
import org.onexus.collection.api.utils.QueryUtils;
import org.onexus.resource.api.IResourceManager;
import org.onexus.resource.api.ORI;
import org.onexus.resource.api.exceptions.OnexusException;
import org.onexus.resource.api.session.LoginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OqlServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OqlServlet.class);
    private IResourceManager resourceManager;
    private ICollectionManager collectionManager;
    private IQueryParser queryParser;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        initLoginContext(req);

        String oqlQuery = req.getParameter("query");
        String filename = req.getParameter("filename");

        try {
            response(resp, oqlQuery, filename, true);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        initLoginContext(req);

        try {
            BufferedReader reader = new BufferedReader(req.getReader());

            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            reader.close();
            if (sb.length() > 0) {
                response(resp, sb.toString(), null, false);
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing query");
            }

        } catch (Exception e) {
            LOGGER.error("Query service", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    public void response(HttpServletResponse resp, String oqlQuery, String filename, boolean useLabels) throws ServletException, IOException {

        if (filename != null) {
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        }

        PrintWriter pw = resp.getWriter();

        Query query;
        try {
            query = queryParser.parseQuery(oqlQuery);
        } catch (Exception e) {
            throw new OnexusException("Error parsing OQL. " + oqlQuery, e);
        }

        IEntityTable result;
        try {
            result = collectionManager.load(query);
        } catch (Exception e) {
            throw new OnexusException("Error loading OQL. " + oqlQuery, e);
        }

        writeHeader(pw, result, useLabels);

        while (result.next()) {
            writeRow(pw, result);
        }

        result.close();
        pw.close();
        resp.flushBuffer();
    }

    private void writeHeader(PrintWriter response, IEntityTable table, boolean useLabels) {

        Iterator<Map.Entry<String, List<String>>> selectIt = table.getQuery().getSelect().entrySet().iterator();
        while (selectIt.hasNext()) {
            Map.Entry<String, List<String>> select = selectIt.next();

            ORI collectionUri = QueryUtils.getCollectionOri(table.getQuery(), select.getKey());
            Collection collection = resourceManager.load(Collection.class, collectionUri);

            Iterator<String> fieldId = select.getValue().iterator();
            while (fieldId.hasNext()) {
                Field field = collection.getField(fieldId.next());
                if (field == null) {
                    continue;
                }

                String label;
                if (useLabels) {
                    label = field.getLabel();
                    if (label == null) {
                        label = field.getId();
                    }
                } else {
                    label = select.getKey() + "." + field.getId();
                }

                response.write(label);

                if (fieldId.hasNext() || selectIt.hasNext()) {
                    response.write("\t");
                }
            }

        }


        response.write("\n");
    }

    private static void writeRow(PrintWriter response, IEntityTable table) {

        Iterator<Map.Entry<String, List<String>>> selectIt = table.getQuery().getSelect().entrySet().iterator();
        while (selectIt.hasNext()) {
            Map.Entry<String, List<String>> select = selectIt.next();

            ORI collection = QueryUtils.getCollectionOri(table.getQuery(), select.getKey());
            IEntity entity = table.getEntity(collection);

            Iterator<String> fieldId = select.getValue().iterator();
            while (fieldId.hasNext()) {
                response.write(String.valueOf(entity.get(fieldId.next())));
                if (fieldId.hasNext() || selectIt.hasNext()) {
                    response.write("\t");
                }
            }
        }

        response.write("\n");
    }

    public ICollectionManager getCollectionManager() {
        return collectionManager;
    }

    public void setCollectionManager(ICollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public IResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public IQueryParser getQueryParser() {
        return queryParser;
    }

    public void setQueryParser(IQueryParser queryParser) {
        this.queryParser = queryParser;
    }

    private void initLoginContext(HttpServletRequest req) {
        HttpSession session = req.getSession();
        if (session != null && LoginContext.get(session.getId()) != null) {
            LoginContext ctx = LoginContext.get(session.getId());
            LoginContext.set(ctx, null);
        } else {
            LoginContext.set(LoginContext.ANONYMOUS_CONTEXT, null);
        }
    }
}
