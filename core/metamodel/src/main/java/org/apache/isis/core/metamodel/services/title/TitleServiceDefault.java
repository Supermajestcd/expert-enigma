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

package org.apache.isis.core.metamodel.services.title;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.val;

@Service
@Named("isisMetaModel.TitleServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class TitleServiceDefault implements TitleService {

    private final WrapperFactory wrapperFactory;
    private final SpecificationLoader specificationLoader;
    
    @Inject
    public TitleServiceDefault(WrapperFactory wrapperFactory, SpecificationLoader specificationLoader) {
        this.wrapperFactory = wrapperFactory;
        this.specificationLoader = specificationLoader;
    }

    @Override
    public String titleOf(final Object domainObject) {
        
        if(specificationLoader == null) { // simplified JUnit test support
            return "" + domainObject;
        }
        
        val pojo = unwrapped(domainObject);
        val objectAdapter = ManagedObject.of(specificationLoader::loadSpecification, pojo);
        val destroyed = ManagedObject._isDestroyed(objectAdapter);
        if(!destroyed) {
            return objectAdapter.getSpecification().getTitle(null, objectAdapter);
        } else {
            return "[DELETED]";
        }
    }

    @Override
    public String iconNameOf(final Object domainObject) {
        
        if(specificationLoader == null) { // simplified JUnit test support 
            return domainObject!=null ? domainObject.getClass().getSimpleName() : "null";
        }
        
        val pojo = unwrapped(domainObject);
        val objectAdapter = ManagedObject.of(specificationLoader::loadSpecification, pojo);
        return objectAdapter.getSpecification().getIconName(objectAdapter);
    }

    //-- HELPER

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }

}
