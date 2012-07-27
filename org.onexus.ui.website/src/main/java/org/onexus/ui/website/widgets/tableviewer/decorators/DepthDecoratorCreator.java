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
package org.onexus.ui.website.widgets.tableviewer.decorators;

import org.onexus.collection.api.Collection;
import org.onexus.collection.api.Field;
import org.onexus.ui.website.widgets.tableviewer.decorators.basic.decorators.ColorDecorator;
import org.onexus.ui.website.widgets.tableviewer.decorators.scales.IColorScaleHtml;
import org.onexus.ui.website.widgets.tableviewer.decorators.scales.LinearColorScale;

import java.awt.*;

public class DepthDecoratorCreator implements IDecoratorCreator {

    private final static IColorScaleHtml depthScale = new LinearColorScale(10.0, 100.0, new Color(255, 255, 255), new Color(0, 255, 0));

    @Override
    public String getDecoratorId() {
        return "DEPTH";
    }

    @Override
    public IDecorator createDecorator(Collection collection, Field columnField, String[] parameters) {
        return new ColorDecorator(columnField, depthScale, "st", true);
    }
}
