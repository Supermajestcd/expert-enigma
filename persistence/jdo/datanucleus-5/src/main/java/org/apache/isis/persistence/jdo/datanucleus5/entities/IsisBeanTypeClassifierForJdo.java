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
package org.apache.isis.persistence.jdo.datanucleus5.entities;

import javax.jdo.annotations.EmbeddedOnly;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.core.config.beans.IsisBeanTypeClassifier;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry.BeanClassification;
import org.apache.isis.core.metamodel.facets.Annotations;

import static org.apache.isis.core.commons.internal.reflection._Annotations.findNearestAnnotation;

import lombok.val;

/**
 * ServiceLoader plugin, classifies PersistenceCapable types into BeanSort.ENTITY.
 * @since 2.0
 */
public class IsisBeanTypeClassifierForJdo implements IsisBeanTypeClassifier {

    @Override
    public BeanClassification classify(Class<?> type) {
        
        val persistenceCapableAnnot = findNearestAnnotation(type, javax.jdo.annotations.PersistenceCapable.class);
        if(persistenceCapableAnnot.isPresent()) {
        
            val embeddedOnlyAttribute = persistenceCapableAnnot.get().embeddedOnly();
            // Whether objects of this type can only be embedded, 
            // hence have no ID that binds them to the persistence layer
            final boolean embeddedOnly = Boolean.valueOf(embeddedOnlyAttribute)
                    || Annotations.getAnnotation(type, EmbeddedOnly.class)!=null; 
            if(embeddedOnly) {
                return null; // don't categorize as entity ... fall through in the caller's logic
            }
            
            return BeanClassification.selfManaged(BeanSort.ENTITY);
        }
        
        return null; // we don't feel responsible to classify given type
    }

}
