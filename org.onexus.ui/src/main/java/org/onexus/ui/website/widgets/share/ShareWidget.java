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
package org.onexus.ui.website.widgets.share;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.onexus.ui.website.Website;
import org.onexus.ui.website.WebsiteStatus;
import org.onexus.ui.website.events.EventQueryUpdate;
import org.onexus.ui.website.widgets.Widget;

import javax.servlet.http.HttpServletRequest;

public class ShareWidget extends Widget<ShareWidgetConfig, ShareWidgetStatus> {


    public ShareWidget(String componentId, IModel<ShareWidgetStatus> statusModel) {
        super(componentId, statusModel);

        onEventFireUpdate(EventQueryUpdate.class);
    }


    @Override
    protected void onBeforeRender() {

        Website website = findParent(Website.class);
        PageParameters params = new PageParameters();

        if (website != null) {
            WebsiteStatus status = website.getStatus();
            status.encodeParameters(params);

            // If the website URI is not defined at Application level then add it as a parameter.
            if (Application.get().getMetaData(Website.WEBSITE_CONFIG) == null) {
                params.add(Website.PARAMETER_WEBSITE, website.getConfig().getURI());
            }

        }

        String linkURL = toAbsolutePath(urlFor(getPage().getClass(), params));
        WebMarkupContainer link = new WebMarkupContainer("link");
        link.add(new AttributeModifier("value", linkURL));
        addOrReplace(link);

        PageParameters embedParams = new PageParameters(params);
        embedParams.set("embed", true);

        String embedHTML = toAbsolutePath(urlFor(getPage().getClass(), embedParams));
        WebMarkupContainer embed = new WebMarkupContainer("embed");
        embed.add(new AttributeModifier("value", embedHTML));
        addOrReplace(embed);

        super.onBeforeRender();

    }

    public final static String toAbsolutePath(final CharSequence relativePagePath) {
        HttpServletRequest req = (HttpServletRequest)((WebRequest)RequestCycle.get().getRequest()).getContainerRequest();
        return RequestUtils.toAbsolutePath(req.getRequestURL().toString(), relativePagePath.toString());
    }

}
