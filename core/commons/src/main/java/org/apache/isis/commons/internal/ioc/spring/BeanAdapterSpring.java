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
package org.apache.isis.commons.internal.ioc.spring;

import org.springframework.beans.factory.ObjectProvider;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;

import lombok.Value;
import lombok.val;

@Value(staticConstructor="of")
final class BeanAdapterSpring implements ManagedBeanAdapter {

    private final String id;
    private final Class<?> beanClass;
    private final ObjectProvider<?> beanProvider;

    @Override
    public Can<?> getInstance() {
        val allMatchingBeans = beanProvider.stream()
                .collect(Can.toCan());
        return allMatchingBeans;
    }

    @Override
    public boolean isCandidateFor(Class<?> requiredType) {
        return beanProvider.stream()
                .map(Object::getClass)
                .anyMatch(requiredType::isAssignableFrom);
    }



}
