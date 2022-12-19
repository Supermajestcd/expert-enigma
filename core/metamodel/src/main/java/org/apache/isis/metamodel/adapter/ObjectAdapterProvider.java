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
package org.apache.isis.metamodel.adapter;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

/**
 * 
 * @since 2.0
 *
 */
public interface ObjectAdapterProvider {

    // -- INTERFACE

    /**
     * @param pojo
     * @return oid for the given domain object 
     */
    default @Nullable Oid oidFor(@Nullable Object pojo) {
        return mapIfPresentElse(adapterFor(pojo), ObjectAdapter::getOid, null);
    }

    /**
     * @return standalone (value) or root adapter
     */
    @Nullable ObjectAdapter adapterFor(@Nullable Object pojo);

    @Nullable ObjectAdapter adapterForBean(@Nullable ManagedBeanAdapter beanAdapter);

    /**
     * @return collection adapter.
     */
    ObjectAdapter adapterForCollection(
            Object domainObject,
            RootOid parentOid,
            OneToManyAssociation collection);

    /**
     * Returns an ObjectAdapter that holds the ObjectSpecification used for 
     * interrogating the domain object's metadata. 
     * <p>
     * Does _not_ perform dependency injection on the domain object. Also bypasses 
     * caching (if any), that is each call to this method creates a new unique instance.
     * </p>
     * 
     * @param viewModelPojo domain object
     * @return  
     */
    ManagedObject disposableAdapterForViewModel(Object viewModelPojo);

    ObjectSpecification specificationForViewModel(Object viewModelPojo);

    ObjectAdapter adapterForViewModel(Object viewModelPojo, String mementoStr);


    // -- DOMAIN OBJECT CREATION SUPPORT

    /**
     * <p>
     * Creates a new instance of the specified type and returns it.
     *
     * <p>
     * The returned object will be initialized (had the relevant callback
     * lifecycle methods invoked).
     *
     * <p>
     * While creating the object it will be initialized with default values and
     * its created lifecycle method (its logical constructor) will be invoked.
     *
     */
    ObjectAdapter newTransientInstance(ObjectSpecification objectSpec);

    //    @Nullable ObjectAdapter recreateViewModelInstance(ObjectSpecification objectSpec, @Nullable final String memento);

    // -- SERVICE LOOKUP 

    Stream<ObjectAdapter> streamServices();
    ObjectAdapter lookupService(String serviceId);


    // -- FOR THOSE THAT IMPLEMENT THROUGH DELEGATION

    public static interface Delegating extends ObjectAdapterProvider {

        ObjectAdapterProvider getObjectAdapterProvider();

        @Override
        default ObjectAdapter adapterFor(Object domainObject) {
            return getObjectAdapterProvider().adapterFor(domainObject);
        }

        @Override
        default ObjectAdapter adapterForBean(ManagedBeanAdapter bean) {
            return getObjectAdapterProvider().adapterForBean(bean);
        }

        @Override
        default ObjectAdapter adapterForCollection(
                final Object pojo,
                final RootOid parentOid,
                OneToManyAssociation collection) {
            return getObjectAdapterProvider().adapterForCollection(pojo, parentOid, collection);
        }

        @Override
        default ManagedObject disposableAdapterForViewModel(Object viewModelPojo) {
            return getObjectAdapterProvider().disposableAdapterForViewModel(viewModelPojo);
        }

        @Override
        default ObjectSpecification specificationForViewModel(Object viewModelPojo) {
            return getObjectAdapterProvider().specificationForViewModel(viewModelPojo);
        }

        @Override
        default ObjectAdapter adapterForViewModel(final Object viewModelPojo, final String mementoString) {
            return getObjectAdapterProvider().adapterForViewModel(viewModelPojo, mementoString);
        }


        @Override
        default ObjectAdapter newTransientInstance(ObjectSpecification objectSpec) {
            return getObjectAdapterProvider().newTransientInstance(objectSpec);
        }


        //        default ObjectAdapter recreateViewModelInstance(ObjectSpecification objectSpec, final String memento) {
        //            return getObjectAdapterProvider().recreateViewModelInstance(objectSpec, memento);
        //        }


        @Override
        default Stream<ObjectAdapter> streamServices() {
            return getObjectAdapterProvider().streamServices();
        }

        @Override
        default ObjectAdapter lookupService(String serviceId) {
            return getObjectAdapterProvider().lookupService(serviceId);
        }


    }


}
