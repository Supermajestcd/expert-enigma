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
package org.apache.isis.metamodel;

import java.util.function.Consumer;

import org.springframework.beans.factory.InjectionPoint;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.metamodel.services.ServiceInjectorDefault;
import org.apache.isis.metamodel.specloader.InjectorMethodEvaluatorDefault;

import static java.util.Objects.requireNonNull;

import lombok.val;

class ServiceInjector_forTesting implements ServiceInjector {

    private ServiceInjector delegate;

    @Override
    public <T> T injectServicesInto(T domainObject, Consumer<InjectionPoint> onNotResolvable) {

        if(delegate==null) {

            // lookup the MetaModelContextBean's list of singletons
            val mmc = MetaModelContext.current();
            if(!(mmc instanceof MetaModelContext_forTesting)) {
                return null;
            }

            val mmcb = (MetaModelContext_forTesting) mmc;

            val configuration = requireNonNull(mmcb.getConfiguration());
            val serviceRegistry = requireNonNull(mmcb.getServiceRegistry());
            val injectorMethodEvaluator = new InjectorMethodEvaluatorDefault();

            //Note: when testing we don't report un-resolvable injection points.

            delegate = ServiceInjectorDefault.getInstanceAndInit(
                    configuration, serviceRegistry, injectorMethodEvaluator);

        }

        return delegate.injectServicesInto(domainObject);
    }

}
