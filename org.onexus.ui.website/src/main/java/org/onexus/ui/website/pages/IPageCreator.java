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
package org.onexus.ui.website.pages;

import org.apache.wicket.model.IModel;
import org.onexus.ui.core.IResourceRegister;

public interface IPageCreator {

    public void register(IResourceRegister resourceRegister);

    public boolean canCreate(PageConfig config);

    public String getTitle();

    public String getDescription();

    public Page<?, ?> create(String componentId, IModel<?> statusModel);

}