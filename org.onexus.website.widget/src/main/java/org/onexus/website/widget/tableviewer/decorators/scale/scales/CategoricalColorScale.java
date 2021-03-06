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
package org.onexus.website.widget.tableviewer.decorators.scale.scales;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public class CategoricalColorScale implements IColorScaleHtml, Serializable {

    private Color unknownColor;
    private Map<String, Color> colors;

    public CategoricalColorScale(Color unknownColor, Map<String, Color> colors) {
        this.unknownColor = unknownColor;
        this.colors = colors;
    }

    @Override
    public Color valueColor(Object value) {

        String key = String.valueOf(value).trim();

        if (colors.containsKey(key)) {
            return colors.get(key);
        }

        return unknownColor;
    }

    @Override
    public String valueRGBHtmlColor(Object value) {
        Color color = valueColor(value);
        return ColorUtils.colorToRGBHtml(color);
    }

    @Override
    public String valueHexHtmlColor(Object value) {
        Color color = valueColor(value);
        return ColorUtils.colorToHexHtml(color);
    }

}
