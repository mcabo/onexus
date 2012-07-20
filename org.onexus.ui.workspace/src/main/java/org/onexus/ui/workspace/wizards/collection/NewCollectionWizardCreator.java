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
package org.onexus.ui.workspace.wizards.collection;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.onexus.resource.api.Folder;
import org.onexus.resource.api.Resource;
import org.onexus.ui.core.wizards.IWizardCreator;

public class NewCollectionWizardCreator implements IWizardCreator {

    @Override
    public String getLabel() {
        return "New collection";
    }

    @Override
    public String getTitle() {
        return "New collection";
    }

    @Override
    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Panel getPanel(String containerId, IModel<? extends Resource> model) {
        return new NewCollectionWizard(containerId, model);
    }

    @Override
    public double getOrder() {
        return 0;
    }

    @Override
    public boolean isVisible(Resource resource) {
        return Folder.class.isAssignableFrom(resource.getClass());
    }

}