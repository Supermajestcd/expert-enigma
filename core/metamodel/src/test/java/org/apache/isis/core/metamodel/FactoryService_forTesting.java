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
package org.apache.isis.core.metamodel;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import org.springframework.beans.factory.InjectionPoint;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.ServiceInjectorLegacy;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
class FactoryService_forTesting implements FactoryService {

    private final MetaModelContext metaModelContext;

    @SneakyThrows
    @Override
    public <T> T getOrCreate(Class<T> requiredType) {
        return requiredType.newInstance();
    }

    @SneakyThrows
    @Override
    public <T> T get(Class<T> requiredType) {
        return requiredType.newInstance();
    }

    @SneakyThrows
    @Override
    public <T> T detachedEntity(Class<T> domainClass) {
        return domainClass.newInstance();
    }

    @Override
    public <T> T mixin(Class<T> mixinClass, Object mixedIn) {
        throw new IllegalArgumentException("Not yet supported");
    }

    @Override
    public <T> T viewModel(Class<T> viewModelClass, @Nullable String mementoStr) {
        throw new IllegalArgumentException("Not yet supported");
    }

    @SneakyThrows
    @Override
    public <T> T create(Class<T> domainClass) {
        return domainClass.newInstance();
    }
}
