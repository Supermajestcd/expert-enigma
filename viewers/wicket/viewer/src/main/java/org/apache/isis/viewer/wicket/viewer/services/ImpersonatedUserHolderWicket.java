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
package org.apache.isis.viewer.wicket.viewer.services;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.user.ImpersonatedUserHolder;
import org.apache.isis.applib.services.user.UserMemento;

/**
 * Implementation that supports impersonation, using the {@link HttpSession}
 * to store the value.
 *
 * @since 2.0 {@index}
 */
@Service
@Named("isis.runtimeservices.ImpersonatedUserHolderWicket")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("HttpSession")
public class ImpersonatedUserHolderWicket implements ImpersonatedUserHolder {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Inject private final HttpSession httpSession;

    private static final String HTTP_SESSION_KEY_IMPERSONATED_USER = ImpersonatedUserHolderWicket.class.getName() + "#userMemento";

    public ImpersonatedUserHolderWicket(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public boolean supportsImpersonation() {
        return true;
    }

    public void setUserMemento(final UserMemento userMemento) {
        this.httpSession.setAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER, userMemento);
    }

    public Optional<UserMemento> getUserMemento() {
        final Object attribute = this.httpSession.getAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER);
        return attribute instanceof UserMemento
                ? Optional.of((UserMemento)attribute)
                : Optional.empty();
    }

    public void clearUserMemento() {
        this.httpSession.removeAttribute(HTTP_SESSION_KEY_IMPERSONATED_USER);
    }
}
