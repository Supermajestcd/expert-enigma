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
package org.apache.isis.viewer.restfulobjects.viewer.webmodule;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationSessionStrategyBasicAuth;
import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.base._Strings.prefix;
import static org.apache.isis.commons.internal.base._Strings.suffix;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;
import static org.apache.isis.commons.internal.resources._Resources.putRestfulPath;

import lombok.val;

/**
 * WebModule that provides the RestfulObjects Viewer.
 * 
 * @since 2.0
 */
@DomainService(
        nature = NatureOfService.DOMAIN, 
        objectType = "restfulObjectsViewer.WebModule") // add to meta-model, for swagger-menu to check whether available or not
@Order(-80)
public final class WebModuleRestfulObjects implements WebModule  {

    private final static String RESTEASY_BOOTSTRAPPER = "org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap";
    private final static String RESTEASY_DISPATCHER = "RestfulObjectsRestEasyDispatcher";

    private final IsisConfiguration isisConfiguration;
    private final String restfulPathConfigValue;

    @Inject
    public WebModuleRestfulObjects(final IsisConfiguration isisConfiguration) {
        this.isisConfiguration = isisConfiguration;
        this.restfulPathConfigValue = isisConfiguration.getViewer().getRestfulobjects().getBasePath();
    }

    @Override
    public String getName() {
        return "RestEasy";
    }

    @Override
    public void prepare(WebModuleContext ctx) {

        if(!isApplicable(ctx)) {
            return;
        }

        putRestfulPath(this.restfulPathConfigValue);

        // register this module as a viewer
        ctx.addViewer("restfulobjects");
        ctx.addProtectedPath(suffix(prefix(this.restfulPathConfigValue, "/"), "/") + "*" );
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        // add IsisSessionFilters

        {
            val filter = ctx.addFilter(
                    "IsisSessionFilterForRestfulObjects", IsisRestfulObjectsSessionFilter.class);

            // this is mapped to the entire application; 
            // however the IsisSessionFilter will 
            // "notice" if the session filter has already been
            // executed for the request pipeline, and if so will do nothing
            filter.addMappingForServletNames(null, true, RESTEASY_DISPATCHER); 

            filter.setInitParameter(
                    "authenticationSessionStrategy", 
                    AuthenticationSessionStrategyBasicAuth.class.getName());
            filter.setInitParameter(
                    "whenNoSession", // what to do if no session was found ...
                    "auto"); // ... 401 and a basic authentication challenge if request originates from web browser
            filter.setInitParameter(
                    "passThru", 
                    String.join(",", getRestfulPath()+"swagger", getRestfulPath()+"health"));

        }

        {
            val filter = ctx.addFilter("RestfulObjectsRestEasyDispatcher",
                    IsisTransactionFilterForRestfulObjects.class.getName());
            filter.addMappingForServletNames(null, true, RESTEASY_DISPATCHER); 
        }



        // add RestEasy

        // used by RestEasy to determine the JAX-RS resources and other related configuration
        ctx.setInitParameter(
                "javax.ws.rs.Application", 
                "org.apache.isis.viewer.restfulobjects.viewer.jaxrsapp.RestfulObjectsApplication");

        ctx.setInitParameter("resteasy.servlet.mapping.prefix", getRestfulPath());

        ctx.addServlet(RESTEASY_DISPATCHER, 
                "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher");
        ctx.getServletRegistration(RESTEASY_DISPATCHER)
        .addMapping(getUrlPattern());

        try {
            final Class<?> listenerClass = getDefaultClassLoader().loadClass(RESTEASY_BOOTSTRAPPER);
            return ctx.createListener(uncheckedCast(listenerClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }

    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        try {
            getDefaultClassLoader().loadClass(RESTEASY_BOOTSTRAPPER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // -- HELPER

    private String getUrlPattern() {
        return getRestfulPath() + "*";
    }

    private String getRestfulPath() {
        requireNonNull(restfulPathConfigValue, "This web-module needs to be prepared first.");
        final String restfulPathEnclosedWithSlashes = suffix(prefix(restfulPathConfigValue, "/"), "/");
        return restfulPathEnclosedWithSlashes;
    }

}
