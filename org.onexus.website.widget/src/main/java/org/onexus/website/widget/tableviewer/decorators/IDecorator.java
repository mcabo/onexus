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
package org.onexus.website.widget.tableviewer.decorators;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;
import org.onexus.collection.api.Collection;
import org.onexus.collection.api.IEntity;

import java.util.List;

/**
 * A IDecorator creates the panel for a given Field and a IEntity instance.
 *
 * @author Jordi Deu-Pons
 */
public interface IDecorator extends IClusterable, IDetachable {

    void populateCell(WebMarkupContainer cellContainer, String componentId,
                      IModel<IEntity> entity);

    String getFormatValue(IEntity entity);

    String getColor(IEntity entity);

    String getTemplate();

    void setTemplate(String template);

    List<String> getExtraFields(Collection collection);

}
