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

package org.apache.isis.core.security.authentication.manager;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationSession;

/**
 * Implementing class is added to {@link ServicesInjector} as an (internal) domain service; 
 * all public methods must be annotated using {@link Programmatic}.
 */
public interface AuthenticationManager extends ApplicationScopedComponent {

    @Programmatic
    void init();

    @Programmatic
    void shutdown();

    /**
     * Caches and returns an authentication {@link AuthenticationSession} if the
     * {@link AuthenticationRequest request} is valid; otherwise returns
     * <tt>null</tt>.
     */
    @Programmatic
    AuthenticationSession authenticate(AuthenticationRequest request);

    @Programmatic
    boolean supportsRegistration(Class<? extends RegistrationDetails> registrationDetailsClass);

    @Programmatic
    boolean register(RegistrationDetails registrationDetails);

    /**
     * Whether the provided {@link AuthenticationSession} is still valid.
     */
    @Programmatic
    boolean isSessionValid(AuthenticationSession authenticationSession);

    @Programmatic
    void closeSession(AuthenticationSession authenticationSession);

}
