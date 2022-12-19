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
package org.apache.isis.core.runtime.session.scope;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.internal.context._Context;

import lombok.NonNull;
import lombok.val;

/**
 * @since 2.0
 */
public class IsisSessionScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        val isisSessionScope = new IsisSessionScope();
        // scope name as defined in annotation @IsisSessionScope
        beanFactory.registerScope("isis-session", isisSessionScope);
        _Context.put(IsisSessionScope.class, isisSessionScope, true);
    }

    public static IsisSessionScopeCloseListener initIsisSessionScopeSupport(
            @NonNull final ServiceInjector serviceInjector) {
        val isisSessionScope = _Context.getElseFail(IsisSessionScope.class);
        serviceInjector.injectServicesInto(isisSessionScope);
        _Context.remove(IsisSessionScope.class); // cleanup
        return isisSessionScope;
    }
}
