/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.facets.collections.javautilcollection;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import static org.apache.isis.core.commons.internal.base._NullSafe.isEmpty;
import static org.apache.isis.core.commons.internal.collections._Arrays.toArray;

import lombok.val;

public class JavaArrayFacet extends CollectionFacetAbstract {

    public JavaArrayFacet(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public Object populatePojo(
            Supplier<Object> emptyCollectionPojoFactory, 
            ObjectSpecification collectionSpec,
            Stream<ManagedObject> initData, 
            int elementCount) {

        final Object[] array = initData
                .map(ManagedObject::getPojo)
                .collect(toArray(Object.class, elementCount));
        return array;
    }

    @Override
    public Stream<ManagedObject> stream(ManagedObject arrayAdapter) {
        final Object[] array = pojoArray(arrayAdapter);
        if(isEmpty(array)) {
            return Stream.of();
        }

        val objectManager = super.getObjectManager();

        return Stream.of(array)
                .map(objectManager::adapt);
    }

    /**
     * Expected to be called with a {@link ManagedObject} wrapping an array.
     */
    @Override
    public int size(final ManagedObject arrayAdapter) {
        return pojoArray(arrayAdapter).length;
    }

    // -- HELPER

    private Object[] pojoArray(final ManagedObject arrayAdapter) {
        return (Object[]) arrayAdapter.getPojo();
    }


}
