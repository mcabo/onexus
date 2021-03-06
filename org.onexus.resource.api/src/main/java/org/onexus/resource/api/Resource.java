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
package org.onexus.resource.api;

import org.onexus.resource.api.annotations.ResourceImplicitList;
import org.onexus.resource.api.annotations.ResourceRegister;
import org.onexus.resource.api.utils.IMetadata;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

@ResourceRegister({Loader.class, Property.class})
public abstract class Resource implements IMetadata, Serializable {

    public final static String PATTERN_ID = "[-A-Za-z0-9+&@#%?=~_|!:,.;]+";

    private ORI ori;

    private String label;

    private String title;

    private String description;

    @Valid
    @ResourceImplicitList("property")
    private List<Property> properties;

    @Valid
    private Loader loader;

    public Resource() {
        super();
    }

    public Resource(ORI ori) {
        super();

        setORI(ori);
    }

    public ORI getORI() {
        return ori;
    }

    public void setORI(ORI ori) {
        this.ori = ori;
    }

    public String getName() {
        String oriStr = ori.toString();
        int lsep = oriStr.lastIndexOf('/');
        int lquestion = oriStr.indexOf('?');
        int last = lquestion != -1 && lquestion > lsep ? lquestion : lsep;
        return oriStr.substring(last + 1);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Loader getLoader() {
        return loader;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getProperty(String propertyKey) {
        if (properties != null) {
            for (Property property : properties) {
                if (property.getKey().equals(propertyKey)) {
                    return property.getValue();
                }
            }
        }

        return null;
    }

    @Override
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return ori.toString();
    }
}
