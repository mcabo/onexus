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
package org.onexus.website.api.utils.error;

import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.pages.AbstractErrorPage;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Generics;
import org.onexus.resource.api.exceptions.OnexusException;
import org.onexus.website.api.Website;
import org.onexus.website.api.theme.DefaultTheme;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class ExceptionErrorPage extends AbstractErrorPage {
    private static final long serialVersionUID = 1L;

    /**
     * Keep a reference to the root cause. WicketTester will use it
     */
    private final transient Throwable throwable;

    /**
     * Constructor.
     *
     * @param throwable The exception to show
     */
    public ExceptionErrorPage(final Throwable throwable) {
        this.throwable = throwable;

        add(new DefaultTheme());

        // Add exception label
        add(new MultiLineLabel("exception", getErrorMessage(throwable)));

        add(new MultiLineLabel("stacktrace", getStackTrace(throwable)));

    }

    /**
     * Converts a Throwable to a string.
     *
     * @param throwable The throwable
     * @return The string
     */
    public String getErrorMessage(final Throwable throwable) {
        if (throwable != null) {
            StringBuilder sb = new StringBuilder(256);

            // first print the last cause or the first onexus exception
            Throwable cause = null;
            List<Throwable> al = convertToList(throwable);
            for (Throwable e : al) {
                if (OnexusException.class.isAssignableFrom(e.getClass())) {
                    cause = e;
                    break;
                }
            }

            if (cause == null) {
                int length = al.size() - 1;
                cause = al.get(length);
            }

            sb.append(cause.getMessage());
            return sb.toString();
        } else {
            return "[Unknown]";
        }
    }

    /**
     * Converts a Throwable to a string.
     *
     * @param throwable The throwable
     * @return The string
     */
    public String getStackTrace(final Throwable throwable) {
        if (throwable != null) {
            List<Throwable> al = convertToList(throwable);

            StringBuilder sb = new StringBuilder(256);

            // first print the last cause
            int length = al.size() - 1;
            Throwable cause = al.get(length);

            outputThrowable(cause, sb, false);

            if (length > 0) {
                sb.append("\n\nComplete stack:\n\n");
                for (int i = 0; i < length; i++) {
                    outputThrowable(al.get(i), sb, true);
                    sb.append("\n");
                }
            }
            return sb.toString();
        } else {
            return "<Null Throwable>";
        }
    }

    /**
     * @param throwable
     * @return xxx
     */
    private List<Throwable> convertToList(final Throwable throwable) {
        List<Throwable> al = Generics.newArrayList();
        Throwable cause = throwable;
        al.add(cause);
        while ((cause.getCause() != null) && (cause != cause.getCause())) {
            cause = cause.getCause();
            al.add(cause);
        }
        return al;
    }

    /**
     * Outputs the throwable and its stacktrace to the stringbuffer. If stopAtWicketSerlvet is true
     * then the output will stop when the org.apache.wicket servlet is reached. sun.reflect.
     * packages are filtered out.
     *
     * @param cause
     * @param sb
     * @param stopAtWicketServlet
     */
    private void outputThrowable(Throwable cause, StringBuilder sb, boolean stopAtWicketServlet) {
        sb.append(cause);
        sb.append("\n");
        StackTraceElement[] trace = cause.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            String traceString = trace[i].toString();
            if (!(traceString.startsWith("sun.reflect.") && i > 1)) {
                sb.append("     at ");
                sb.append(traceString);
                sb.append("\n");
                if (stopAtWicketServlet &&
                        (traceString.startsWith("org.apache.wicket.protocol.http.WicketServlet") || traceString.startsWith("org.apache.wicket.protocol.http.WicketFilter"))) {
                    return;
                }
            }
        }
    }

    @Override
    protected void setHeaders(final WebResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Get access to the exception
     *
     * @return The exception
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    protected void onBeforeRender() {

        if (getThrowable() instanceof StalePageException) {
            setResponsePage(Website.class);
        }

        super.onBeforeRender();
    }
}
