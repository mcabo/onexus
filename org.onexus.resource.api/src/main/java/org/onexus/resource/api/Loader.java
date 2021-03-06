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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ResourceRegister({Parameter.class})
public class Loader implements Serializable {

    @NotNull
    private String plugin;

    @Valid
    @ResourceImplicitList("parameter")
    private List<Parameter> parameters;

    public Loader() {
        super();
    }

    public Loader(String plugin, List<Parameter> parameters) {
        super();
        this.plugin = plugin;
        this.parameters = parameters;
    }


    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }


    public String getParameter(String key) {
        for (Parameter value : parameters) {
            if (value.getKey().equals(key)) {
                return value.getValue();
            }
        }
        return null;
    }

    public List<String> getParameterList(String key) {
        List<String> result = new ArrayList<String>();
        for (Parameter value : parameters) {
            if (value.getKey().equals(key)) {
                result.add(value.getValue());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Loader{" +
                "plugin='" + plugin + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
