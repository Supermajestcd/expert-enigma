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

package org.apache.isis.runtime.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Cardinality;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.memento.ObjectAdapterMementoDefault.RecreateStrategy;

import lombok.val;

/**
 * @since 2.0
 */
public interface ObjectAdapterMemento extends Serializable {

    UUID getStoreKey();
    BeanSort getBeanSort();
    RootOid getRootOid();

    String asString();

    /**
     * TODO[2112] outdated
     * Returns a bookmark only if {@link RecreateStrategy#LOOKUP} and 
     * {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}.
     * Returns {@code null} otherwise. 
     */
    Bookmark asBookmarkIfSupported();

    /**
     * TODO[2112] outdated 
     * Returns a bookmark only if {@link RecreateStrategy#LOOKUP} and 
     * {@link #getCardinality() sort} is {@link Cardinality#SCALAR scalar}.
     * Returns {@code null} otherwise. 
     */
    Bookmark asHintingBookmarkIfSupported();

    ObjectSpecId getObjectSpecId();

    ObjectAdapter getObjectAdapter(SpecificationLoader specificationLoader); 

    // -- FACTORIES

    static ObjectAdapterMemento ofRootOid(RootOid rootOid, ObjectAdapterMementoSupport support) {
        if(rootOid==null) {
            return null;
        }
        return support.mementoForRootOid(rootOid);
    }

    static ObjectAdapterMemento ofAdapter(ManagedObject adapter, ObjectAdapterMementoSupport support) {
        if(adapter==null) {
            return null;
        }
        return support.mementoForAdapter(adapter);
    }

    static ObjectAdapterMemento ofPojo(Object pojo, ObjectAdapterMementoSupport support) {
        if(pojo==null) {
            return null;
        }
        return support.mementoForPojo(pojo);
    }

    static ObjectAdapterMemento wrapMementoList(
            Collection<ObjectAdapterMemento> container, 
            ObjectSpecId specId) {

        // ArrayList is serializable
        if(container instanceof ArrayList) {
            return ObjectAdapterMementoCollection.of((ArrayList<ObjectAdapterMemento>)container, specId);
        }
        return ObjectAdapterMementoCollection.of(_Lists.newArrayList(container), specId);
    }

    // ArrayList is serializable
    static Optional<ArrayList<ObjectAdapterMemento>> unwrapList(ObjectAdapterMemento memento) {
        if(memento==null) {
            return Optional.empty();
        }
        if(!(memento instanceof ObjectAdapterMementoCollection)) {
            return Optional.empty();
        }
        return Optional.ofNullable(((ObjectAdapterMementoCollection)memento).unwrapList());
    }


    static ObjectAdapterMemento ofIterablePojos(
            Object iterablePojos,
            ObjectSpecId specId,
            ObjectAdapterMementoSupport support) {

        val listOfMementos = _NullSafe.stream((Iterable<?>) iterablePojos)
                .map(pojo->ofPojo(pojo, support))
                .collect(Collectors.toList());
        val memento =
                ObjectAdapterMemento.wrapMementoList(listOfMementos, specId);
        return memento;
    }


}
