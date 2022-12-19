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
package org.apache.isis.testdomain.transactions.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.JpaTestDomainPersona;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

/**
 * These tests use the {@code @Transactional} annotation as provided by Spring.
 * <p> 
 * We test whether JUnit Tests are automatically rolled back by Spring. 
 */
@SpringBootTest(
        classes = { 
                Configuration_usingJpa.class,
        },
        properties = {
                "logging.level.org.apache.isis.core.runtimeservices.session.InteractionFactoryDefault=DEBUG",
                "logging.level.org.apache.isis.persistence.*=DEBUG",
                "logging.level.org.springframework.test.context.transaction.*=DEBUG"
        })  
@Transactional
@TestPropertySource(IsisPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaTransactionRollbackTest_usingTransactional 
//extends IsisIntegrationTestAbstract 
{
    
    @Inject private FixtureScripts fixtureScripts;
    @Inject private RepositoryService repository;
    
    @Test @Order(1) @Commit
    void cleanup_justInCase() {
        // cleanup just in case
        fixtureScripts.runPersona(JpaTestDomainPersona.PurgeAll);
    }
    
    @Test @Order(2)
    void happyCaseTx_shouldCommit() {
        
        // expected pre condition
        assertEquals(0, repository.allInstances(JpaBook.class).size());
        
        _Probe.errOut("before fixture");
            
        fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);
        
        _Probe.errOut("after fixture");
        
        // expected post condition
        assertEquals(1, repository.allInstances(JpaBook.class).size());
        
    }
    
    @Test @Order(3)
    void previousTest_shouldHaveBeenRolledBack() {
        
        // expected condition
        assertEquals(0, repository.allInstances(JpaBook.class).size());
    }

}
