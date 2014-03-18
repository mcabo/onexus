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
package org.onexus.collection.store.elasticsearch.internal.filters;

import org.elasticsearch.index.query.FilterBuilder;
import org.onexus.collection.api.query.BinaryFilter;
import org.onexus.collection.api.query.Filter;
import org.onexus.collection.api.query.Query;
import org.onexus.resource.api.IResourceManager;

import static org.onexus.collection.store.elasticsearch.internal.filters.FilterAdapterFactory.filterAdapter;

public abstract class BinaryFilterAdapter<T extends BinaryFilter> extends AbstractFilterAdapter<T> {

    public BinaryFilterAdapter(Class<T> type) {
        super(type);
    }

    @Override
    protected FilterBuilder innerBuild(IResourceManager resourceManager, Query query, T filter) {

        Filter leftFilter = filter.getLeft();
        Filter rightFilter = filter.getRight();

        FilterAdapter leftAdapter = filterAdapter(leftFilter);
        FilterAdapter rightAdapter = filterAdapter(rightFilter);

        FilterBuilder leftBuilder = leftAdapter.build(resourceManager, query, leftFilter);
        FilterBuilder rightBuilder = rightAdapter.build(resourceManager, query, rightFilter);

        return innerBuild(leftBuilder, rightBuilder, filter);
    }

    protected abstract FilterBuilder innerBuild(FilterBuilder left, FilterBuilder right, T filter);
}
