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
package org.apache.isis.core.metamodel.objectmanager;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.core.metamodel.objectmanager.detach.ObjectDetacher;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectIdentifier;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.objectmanager.refresh.ObjectRefresher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Bundles all domain object state related responsibilities:<br>
 * - object creation ... init defaults <br>
 * - object loading ... given a specific object identifier (id) <br>
 * - object identification ... given a domain object (pojo) <br>
 * - object refreshing ... given a domain object (pojo) <br>
 *  
 * @since 2.0
 */
public interface ObjectManager {

    MetaModelContext getMetaModelContext();
    
    ObjectCreator getObjectCreator();
    ObjectLoader getObjectLoader();
    ObjectIdentifier getObjectIdentifier();
    ObjectRefresher getObjectRefresher();
    ObjectDetacher getObjectDetacher();

    // -- SHORTCUTS

    /**
     * Creates and initializes an instance conforming to given request parameters.
     * @param objectCreateRequest
     * @return
     */
    public default ManagedObject createObject(ObjectCreator.Request objectCreateRequest) {
        return getObjectCreator().createObject(objectCreateRequest);
    }
    
    /**
     * Loads an instance identified with given request parameters.
     * @param objectLoadRequest
     * @return
     */
    public default ManagedObject loadObject(ObjectLoader.Request objectLoadRequest) {
        return getObjectLoader().loadObject(objectLoadRequest);
    }
    
    /**
     * Returns an object identifier for the instance.
     * @param managedObject
     * @return
     */
    public default RootOid identifyObject(ManagedObject managedObject) {
        return getObjectIdentifier().identifyObject(managedObject);
    }
    
    /**
     * Reloads the state of the (entity) instance from the data store.
     * @param managedObject
     */
    public default void refreshObject(ManagedObject managedObject) {
        getObjectRefresher().refreshObject(managedObject);
    }
    
    public default ManagedObject adapt(@Nullable Object pojo) {
        if(pojo==null) {
            return ManagedObject.empty(); 
        }
        return ManagedObject.of(this::loadSpecification, pojo);
    }

    @Nullable
    public default ObjectSpecification loadSpecification(@Nullable Object pojo) {
        if(pojo==null) {
            return null; 
        }
        return loadSpecification(pojo.getClass());
    }
    
    @Nullable
    default ObjectSpecification loadSpecification(@Nullable final Class<?> domainType) {
        return getMetaModelContext().getSpecificationLoader().loadSpecification(domainType);
    }
    
}
