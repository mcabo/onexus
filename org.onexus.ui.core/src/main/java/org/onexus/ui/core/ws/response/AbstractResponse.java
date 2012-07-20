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
package org.onexus.ui.core.ws.response;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.onexus.ui.core.OnexusWebApplication;


public abstract class AbstractResponse extends AbstractResource.ResourceResponse {

    public AbstractResponse() {
        super();
        OnexusWebApplication.inject(this);

        setWriteCallback(new AbstractResource.WriteCallback() {
            @Override
            public void writeData(IResource.Attributes attributes) {
                AbstractResponse.this.writeData(attributes.getResponse());
            }
        });
    }

    protected abstract void writeData(Response response);


}