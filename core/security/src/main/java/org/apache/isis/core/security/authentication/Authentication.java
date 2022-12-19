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

package org.apache.isis.core.security.authentication;

import java.io.Serializable;
import java.util.Optional;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.NonNull;

/**
 * An immutable, serializable value type, that holds details about a user's authentication.
 *
 * <p>
 *     This is really little more than a thin wrapper around {@link InteractionContext},
 *     surfaces a number of the security-specific attributes of that field.
 * </p>
 *
 * @apiNote This is a framework internal class and so does not constitute a formal API.
 *
 * @since 2.0 {@index}
 */
public interface Authentication extends Serializable {

    String INTERACTION_CONTEXT_KEY = Authentication.class.getName();

    /**
     * The name of the authenticated user; for display purposes only.
     */
    default String getUserName() {
        return getUser().getName();
    }

    /**
     * A unique code given to this session during authentication.
     *
     * <p>
     * This can be used to confirm that this session has been properly created
     * and the user has been authenticated. It should return an empty string (
     * <tt>""</tt>) if this is unauthenticated user
     */
    String getValidationCode();

    /**
     * The (programmatically) simulated (or actual) user, belonging to this session.
     *
     * @apiNote immutable, allows an {@link Interaction} to (logically) run with its
     * own simulated (or actual) user
     */
    default UserMemento getUser() {
        return getInteractionContext().getUser();
    }

    /**
     * The {@link InteractionContext} (programmatically) simulated (or actual), belonging to this session.
     *
     * @apiNote immutable, allows an {@link Interaction} to (logically) run with its
     * own simulated (or actual) clock
     */
    InteractionContext getInteractionContext();

    /**
     * To support external security mechanisms such as keycloak, where the validity of the session is defined by
     * headers in the request.
     */
    default Type getType() {
        return Type.DEFAULT;
    }

    public enum Type {
        DEFAULT,
        /**
         * Instructs the {@link org.apache.isis.core.security.authentication.manager.AuthenticationManager} to not cache this session in its internal map of
         * sessions by validation code, and therefore to ignore this aspect when considering if an
         * {@link Authentication} is
         * {@link org.apache.isis.core.security.authentication.manager.AuthenticationManager#isSessionValid(Authentication) valid} or not.
         */
        EXTERNAL
    }

    // -- WITHERS

    /**
     * Returns a copy with given {@code interactionContext}.
     * @param interactionContext
     */
    Authentication withInteractionContext(InteractionContext interactionContext);

    /**
     * Looks up an {@link Authentication} from the provided {@link InteractionContext}.
     *
     * <p>
     *     If the {@link InteractionContext} was created as the result of an authentication process (as opposed to
     *     programmatically) then there will be an attached {@link Authentication} object.
     * </p>
     *
     * @param interactionContext
     * @return
     */
    public static Optional<Authentication> authenticationFrom(final @NonNull InteractionContext interactionContext) {
        return interactionContext.getAttribute(INTERACTION_CONTEXT_KEY, Authentication.class);
    }

}
