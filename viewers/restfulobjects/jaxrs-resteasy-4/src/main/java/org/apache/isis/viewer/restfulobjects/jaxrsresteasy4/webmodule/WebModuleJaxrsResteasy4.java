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
package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;
import org.apache.isis.core.webapp.modules.WebModuleContext;
import org.apache.isis.viewer.restfulobjects.viewer.webmodule.IsisRestfulObjectsSessionFilter;
import org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationSessionStrategyBasicAuth;

import lombok.Getter;
import lombok.val;

/**
 * WebModule that provides the RestfulObjects Viewer.
 * 
 * @since 2.0
 *
 * @implNote CDI feels responsible to resolve injection points for any Servlet or Filter
 * we register programmatically on the ServletContext.
 * As long as injection points are considered to be resolved by Spring, we can workaround this fact:
 * By replacing annotations {@code @Inject} with {@code @Autowire} for any Servlet or Filter,
 * that get contributed by a WebModule, these will be ignored by CDI.
 *
 */
@Service
@Named("isisRoViewer.WebModuleJaxrsRestEasy4")
@Order(OrderPrecedence.MIDPOINT - 80)
@Qualifier("JaxrsRestEasy4")
public final class WebModuleJaxrsResteasy4 extends WebModuleAbstract {

    private static final String ISIS_SESSION_FILTER_FOR_RESTFUL_OBJECTS = "IsisSessionFilterForRestfulObjects";
    //private static final String ISIS_TRANSACTION_FILTER = "IsisTransactionFilterForRestfulObjects";

    private final RestEasyConfiguration restEasyConfiguration;

    private final String restfulPath;
    private final String urlPattern;

    @Inject
    public WebModuleJaxrsResteasy4(
            final RestEasyConfiguration restEasyConfiguration,
            final ServiceInjector serviceInjector) {
        super(serviceInjector);
        this.restEasyConfiguration = restEasyConfiguration;
        this.restfulPath = this.restEasyConfiguration.getJaxrs().getDefaultPath() + "/";
        this.urlPattern = this.restfulPath + "*";
    }

    @Getter
    private final String name = "JaxrsRestEasy4";

    @Override
    public void prepare(WebModuleContext ctx) {

        // forces RuntimeDelegate.getInstance() to be provided by RestEasy
        // (and not by eg. the JEE container if any)
        ResteasyProviderFactory.setInstance(new ResteasyProviderFactoryImpl());

        super.prepare(ctx);

        if(!isApplicable(ctx)) {
            return;
        }

        ctx.addProtectedPath(urlPattern);
    }

    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        val authenticationSessionStrategyClassName = restEasyConfiguration.getAuthentication().getStrategyClassName()
                .orElse(AuthenticationSessionStrategyBasicAuth.class.getName());
        
        registerFilter(ctx, ISIS_SESSION_FILTER_FOR_RESTFUL_OBJECTS, IsisRestfulObjectsSessionFilter.class)
        .ifPresent(filterReg -> {
            // this is mapped to the entire application;
            // however the IsisSessionFilter will
            // "notice" if the session filter has already been
            // executed for the request pipeline, and if so will do nothing
            filterReg.addMappingForUrlPatterns(
                    null,
                    true,
                    this.urlPattern);
            
            filterReg.setInitParameter(
                    "authenticationSessionStrategy",
                    authenticationSessionStrategyClassName);
            filterReg.setInitParameter(
                    "whenNoSession", // what to do if no session was found ...
                    "auto"); // ... 401 and a basic authentication challenge if request originates from web browser
            filterReg.setInitParameter(
                    "passThru",
                    String.join(",",
                            this.restfulPath + "swagger",
                            this.restfulPath + "health"));

        } );

        return Can.empty(); // registers no listeners
    }


}
