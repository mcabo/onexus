/**
 *  Copyright 2011 Universitat Pompeu Fabra.
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
package org.onexus.ui.website.tabs.topleft;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.onexus.ui.website.tabs.Tab;
import org.onexus.ui.website.viewers.ICollectionViewerManager;
import org.onexus.ui.website.viewers.ViewerConfig;

import javax.inject.Inject;

/**
 * @author Jordi Deu-Pons
 */
public class TopleftTab extends Tab<TopleftTabConfig, TopleftTabStatus> {

    @Inject
    public ICollectionViewerManager viewerManager;

    public static final CssResourceReference CSS = new CssResourceReference(TopleftTab.class, "TopleftTab.css");

    public TopleftTab(String panelId, TopleftTabConfig config, IModel<TopleftTabStatus> statusModel) {
        super(panelId, config, statusModel);

        // Add top widgets
        add(new HoritzontalWidgetBar("topwidgets", getConfig().getTopWidgets(), getModelStatus()));

        // Add top-rigth widgets
        add(new HoritzontalWidgetBar("toprightwidgets", getConfig().getTopRightWidgets(), getModelStatus()));

        // Add left widgets
        add(new VerticalWidgetBar("leftwidgets", getConfig().getLeftWidgets(), getModelStatus()));

        // Add viewer
        ViewerConfig viewerConfig = getCurrentViewer();

        add(viewerManager.create("viewer", viewerConfig, new ViewerModel(viewerConfig, statusModel)));

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderCSSReference(CSS);
    }

}
