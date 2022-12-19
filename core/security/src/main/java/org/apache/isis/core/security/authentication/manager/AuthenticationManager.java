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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.exceptions.unrecoverable.NoAuthenticatorException;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.Authenticator;
import org.apache.isis.core.security.authentication.standard.RandomCodeGenerator;
import org.apache.isis.core.security.authentication.standard.Registrar;

import lombok.Getter;
import lombok.val;

@Service
@Named("isis.security.AuthenticationManager")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class AuthenticationManager {

    @Getter private final Can<Authenticator> authenticators;

    private final Map<String, String> userByValidationCode = _Maps.newConcurrentHashMap();
    private final RandomCodeGenerator randomCodeGenerator;
    private final Can<Registrar> registrars;

    @Inject
    public AuthenticationManager(
            final List<Authenticator> authenticators,
            final RandomCodeGenerator randomCodeGenerator) {
        this.randomCodeGenerator = randomCodeGenerator;
        this.authenticators = Can.ofCollection(authenticators);
        if (this.authenticators.isEmpty()) {
            throw new NoAuthenticatorException("No authenticators specified");
        }
        this.registrars = this.authenticators
                .filter(Registrar.class::isInstance)
                .map(Registrar.class::cast);
    }

    // -- SESSION MANAGEMENT (including authenticate)

    public final Authentication authenticate(AuthenticationRequest request) {

        if (request == null) {
            return null;
        }

        val compatibleAuthenticators = authenticators
                .filter(authenticator->authenticator.canAuthenticate(request.getClass()));

        if (compatibleAuthenticators.isEmpty()) {
            throw new NoAuthenticatorException(
                    "No authenticator available for processing " + request.getClass().getName());
        }

        for (val authenticator : compatibleAuthenticators) {
            val authentication = authenticator.authenticate(request, getUnusedRandomCode());
            if (authentication != null) {
                userByValidationCode.put(authentication.getValidationCode(), authentication.getUserName());
                return authentication;
            }
        }

        return null;
    }

    private String getUnusedRandomCode() {

        val stopWatch = _Timing.now();

        String code;
        do {

            // guard against infinite loop when unique code generation for some reason fails
            if(stopWatch.getMillis()>3000L) {
                throw new NoAuthenticatorException(
                        "RandomCodeGenerator failed to produce a unique code within 3s.");
            }

            code = randomCodeGenerator.generateRandomCode();
        } while (userByValidationCode.containsKey(code));

        return code;
    }


    public final boolean isSessionValid(final @Nullable Authentication authentication) {
        if(authentication==null) {
            return false;
        }
        if(authentication.getType() == Authentication.Type.EXTERNAL) {
            return true;
        }
        final String userName = userByValidationCode.get(authentication.getValidationCode());
        return authentication.getUser().isCurrentUser(userName);
    }


    public void closeSession(Authentication authentication) {
        for (val authenticator : authenticators) {
            authenticator.logout(authentication);
        }
        userByValidationCode.remove(authentication.getValidationCode());
    }

    // -- AUTHENTICATORS

    public boolean register(RegistrationDetails registrationDetails) {
        for (val registrar : this.registrars) {
            if (registrar.canRegister(registrationDetails.getClass())) {
                return registrar.register(registrationDetails);
            }
        }
        return false;
    }


    public boolean supportsRegistration(Class<? extends RegistrationDetails> registrationDetailsClass) {
        for (val registrar : this.registrars) {
            if (registrar.canRegister(registrationDetailsClass)) {
                return true;
            }
        }
        return false;
    }



    // -- DEBUGGING

    private static final ToString<AuthenticationManager> toString =
            ToString.<AuthenticationManager>toString("class", obj->obj.getClass().getSimpleName())
            .thenToString("authenticators", obj->""+obj.authenticators.size())
            .thenToString("users", obj->""+obj.userByValidationCode.size());

    @Override
    public String toString() {
        return toString.toString(this);
    }



}
