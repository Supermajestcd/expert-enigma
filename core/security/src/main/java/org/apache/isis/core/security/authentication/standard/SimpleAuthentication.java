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

package org.apache.isis.core.security.authentication.standard;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.core.security.authentication.AuthenticationAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class SimpleAuthentication
extends AuthenticationAbstract {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static SimpleAuthentication of(
            final @NonNull UserMemento user,
            final @NonNull String validationCode) {
        return new SimpleAuthentication(InteractionContext.ofUserWithSystemDefaults(user), validationCode);
    }

    public static SimpleAuthentication validOf(
            final @NonNull UserMemento user) {
        return of(user, DEFAULT_AUTH_VALID_CODE);
    }

    public static SimpleAuthentication validOf(
            final @NonNull InteractionContext interactionContext) {
        return new SimpleAuthentication(interactionContext, DEFAULT_AUTH_VALID_CODE);
    }

    // -- CONSTRUCTOR

    public SimpleAuthentication(
            final @NonNull InteractionContext interactionContext,
            final @NonNull String validationCode) {
        super(interactionContext, validationCode);
    }

    @Getter @Setter
    private Type type = Type.DEFAULT;

}
