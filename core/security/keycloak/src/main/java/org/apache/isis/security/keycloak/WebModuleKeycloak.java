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
package org.apache.isis.security.keycloak;

import lombok.val;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

/**
 * WebModule to enable support for Keycloak.
 */
@Service @Order(Ordered.HIGHEST_PRECEDENCE)
public final class WebModuleKeycloak implements WebModule  {

    public final static ThreadLocal<AuthenticationSession> sessionByThread = new ThreadLocal<>();

    private final static String KEYCLOAK_FILTER_CLASS_NAME =
            KeycloakFilter.class.getName();

    private final static String KEYCLOAK_FILTER_NAME = "KeycloakFilter";

    // -- CONFIGURATION


    // -- 

    @Override
    public String getName() {
        return "Keycloak";
    }
    
    @Override
    public void prepare(WebModuleContext ctx) {
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        final Dynamic filter;
        try {
            val filterClass = getDefaultClassLoader().loadClass(KEYCLOAK_FILTER_CLASS_NAME);
            val filterInstance = ctx.createFilter(uncheckedCast(filterClass));
            filter = ctx.addFilter(KEYCLOAK_FILTER_NAME, filterInstance);
            if(filter==null) {
                return null; // filter was already registered somewhere else (eg web.xml)
            }
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }

        val urlPattern = "/*";
        filter.addMappingForUrlPatterns(null, false, urlPattern); // filter is forced first

        return null;
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        return true;
    }
}
