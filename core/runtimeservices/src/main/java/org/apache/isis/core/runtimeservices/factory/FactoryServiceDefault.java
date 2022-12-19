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

package org.apache.isis.core.runtimeservices.factory;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.session.IsisSessionFactory;

import static org.apache.isis.core.commons.internal.base._With.requires;
import static org.apache.isis.core.commons.internal.reflection._Reflect.Filter.paramAssignableFrom;
import static org.apache.isis.core.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.val;

@Service
@Named("isisRuntimeServices.FactoryServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class FactoryServiceDefault implements FactoryService {
    
    @Inject IsisSessionFactory isisSessionFactory; // dependsOn
    @Inject private SpecificationLoader specificationLoader;
    @Inject private ServiceInjector serviceInjector;

    @Override
    public <T> T instantiate(final Class<T> domainClass) {
        val spec = specificationLoader.loadSpecification(domainClass);
        val adapter = ManagedObject._newTransientInstance(spec); 
        return _Casts.uncheckedCast(adapter.getPojo());
    }

    @Override
    public <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        val objectSpec = specificationLoader.loadSpecification(mixinClass);
        val mixinFacet = objectSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            throw _Exceptions.illegalArgument("Class '%s'  is not a mixin", mixinClass.getName());
        }
        if(!mixinFacet.isMixinFor(mixedIn.getClass())) {
            throw _Exceptions.illegalArgument("Mixin class '%s' is not a mixin for supplied object '%s'",
                    mixinClass.getName(), mixedIn);
        }
        val mixinConstructor = _Reflect
                .getPublicConstructors(mixinClass) 
                        .filter(paramCount(1).and(paramAssignableFrom(0, mixedIn.getClass())))
                .getSingleton()
                .orElseThrow(()->_Exceptions.illegalArgument(
                        "Failed to locate constructor in '%s' to instantiate using '%s'", 
                        mixinClass.getName(), mixedIn));
        
        try {
            val mixin = mixinConstructor.newInstance(mixedIn);
            return _Casts.uncheckedCast(serviceInjector.injectServicesInto(mixin));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw _Exceptions.illegalArgument(
                    "Failed to invoke constructor of '%s' using single argument '%s'", 
                    mixinClass.getName(), mixedIn);
        }
    }

    @Override
    public <T> T viewModel(Class<T> viewModelClass, String mementoStr) {
        requires(viewModelClass, "viewModelClass");

        val spec = specificationLoader.loadSpecification(viewModelClass);
        if (!spec.containsFacet(ViewModelFacet.class)) {
            throw _Exceptions.illegalArgument("Type '%s' must be recogniced as a ViewModel, "
                    + "that is the type's meta-model "
                    + "must have an associated ViewModelFacet: ", viewModelClass.getName());
        }

        val viewModelFacet = spec.getFacet(ViewModelFacet.class);
        val viewModel = viewModelFacet.createViewModelPojo(spec, mementoStr, __->instantiate(viewModelClass));

        return _Casts.uncheckedCast(viewModel);
    }






}
