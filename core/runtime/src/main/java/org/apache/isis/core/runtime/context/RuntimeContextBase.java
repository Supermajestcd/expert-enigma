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
package org.apache.isis.core.runtime.context;

import java.util.function.Supplier;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;

import lombok.Getter;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public abstract class RuntimeContextBase implements RuntimeContext {

    // -- FINAL FIELDS

    @Getter(onMethod = @__(@Override)) protected final MetaModelContext metaModelContext;
    @Getter(onMethod = @__(@Override)) protected final IsisConfiguration configuration;
    @Getter(onMethod = @__(@Override)) protected final ServiceInjector serviceInjector;
    @Getter(onMethod = @__(@Override)) protected final ServiceRegistry serviceRegistry;
    @Getter(onMethod = @__(@Override)) protected final SpecificationLoader specificationLoader;
    @Getter(onMethod = @__(@Override)) protected final IsisInteractionTracker isisInteractionTracker;
    
    @Getter protected final IsisInteractionFactory isisInteractionFactory;
    @Getter protected final AuthenticationManager authenticationManager;
    @Getter protected final TransactionService transactionService;
    @Getter protected final Supplier<ManagedObject> homePageSupplier;
    @Getter protected final ObjectManager objectManager;
    
    // -- SINGLE ARG CONSTRUCTOR

    protected RuntimeContextBase(MetaModelContext mmc) {
        this.metaModelContext= mmc;
        this.configuration = mmc.getConfiguration();
        this.serviceInjector = mmc.getServiceInjector();
        this.serviceRegistry = mmc.getServiceRegistry();
        this.specificationLoader = mmc.getSpecificationLoader();
        this.objectManager = mmc.getObjectManager();
        this.transactionService = mmc.getTransactionService();
        this.homePageSupplier = mmc::getHomePageAdapter;
        this.isisInteractionFactory = serviceRegistry.lookupServiceElseFail(IsisInteractionFactory.class);
        this.authenticationManager = serviceRegistry.lookupServiceElseFail(AuthenticationManager.class);
        this.isisInteractionTracker = serviceRegistry.lookupServiceElseFail(IsisInteractionTracker.class);
    }
    
    // -- AUTH
    
    public AuthenticationSessionTracker getAuthenticationSessionTracker() {
        return isisInteractionTracker;
    }

    @Override
    public void logoutAuthenticationSession() {
        // we do the logout (removes this session from those valid)
        // similar code in wicket viewer (AuthenticatedWebSessionForIsis#onInvalidate())
        
        isisInteractionTracker
        .currentAuthenticationSession()
        .ifPresent(authenticationSession->{
        
            val authenticationManager = getServiceRegistry().lookupServiceElseFail(AuthenticationManager.class);
            authenticationManager.closeSession(authenticationSession);
            
            isisInteractionFactory.closeSessionStack();
            
        });
        
        	
    }


}
