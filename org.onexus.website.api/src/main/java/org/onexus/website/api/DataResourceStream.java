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
package org.onexus.website.api;

import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.onexus.data.api.IDataStreams;

import java.io.IOException;
import java.io.InputStream;

public class DataResourceStream extends AbstractResourceStream {

    private IDataStreams data;

    public DataResourceStream(IDataStreams data) {
        super();

        this.data = data;
    }

    @Override
    public InputStream getInputStream() throws ResourceStreamNotFoundException {
        return data.iterator().next();
    }

    @Override
    public void close() throws IOException {
        data.close();
    }
}
