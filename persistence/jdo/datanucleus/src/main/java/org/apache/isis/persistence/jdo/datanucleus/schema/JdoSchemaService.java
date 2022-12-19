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
package org.apache.isis.persistence.jdo.datanucleus.schema;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.events.MetamodelEvent;
import org.apache.isis.persistence.jdo.datanucleus.config.DnSettings;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.extern.log4j.Log4j2;

/**
 * Hooks into the application's lifecycle and runs database schema creation logic
 * as soon as the meta-model was populated.
 *
 * @since 2.0 {@index}
 */
@Service
@Named("isisJdoDn.JdoSchemaService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class JdoSchemaService {

    @Inject MetaModelContext metaModelContext;
    @Inject TransactionAwarePersistenceManagerFactoryProxy txAwarePmfProxy;

    @Named("jdo-platform-transaction-manager")
    @Inject PlatformTransactionManager txManager;

    @Inject IsisBeanTypeRegistry isisBeanTypeRegistry;
    @Inject DnSettings dnSettings;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled()) {
            log.debug("init entity types {}",
                    txAwarePmfProxy.getPersistenceManagerFactory().getManagedClasses());
        }
    }

    @EventListener(MetamodelEvent.class)
    public void onMetamodelEvent(MetamodelEvent event) {

        log.debug("received metamodel event {}", event);

        switch (event) {
        case BEFORE_METAMODEL_LOADING:
            break;
        case AFTER_METAMODEL_LOADED:
            new _DnApplication(metaModelContext, dnSettings); // creates schema
            break;

        default:
            throw _Exceptions.unmatchedCase(event);
        }

    }


}
