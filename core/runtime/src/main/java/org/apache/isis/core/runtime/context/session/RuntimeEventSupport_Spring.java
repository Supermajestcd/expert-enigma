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
package org.apache.isis.core.runtime.context.session;

import javax.enterprise.event.Event;
import javax.inject.Named;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.core.commons.internal.ioc.spring._Spring;

@Configuration
@Named("isisRuntime.RuntimeEventSupport_Spring")
public class RuntimeEventSupport_Spring {

    @Bean
    public Event<AppLifecycleEvent> appLifecycleEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<IsisSessionLifecycleEvent> sessionLifecycleEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

}
