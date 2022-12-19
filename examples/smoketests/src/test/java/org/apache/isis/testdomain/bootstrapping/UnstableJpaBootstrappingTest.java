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
package org.apache.isis.testdomain.bootstrapping;

import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJpa.class,
        }
)

//@DataJpaTest
//@Import({
//    Configuration_usingJpa.class
//})
@TestPropertySource(IsisPresets.UseLog4j2Test)
////@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Incubating("JPA support is under construction")
class UnstableJpaBootstrappingTest {

    @Inject private Optional<PlatformTransactionManager> platformTransactionManager; 
    @Inject private RepositoryService repository;
    //@Inject private TransactionService transactionService;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @AfterAll
    static void afterAll() throws SQLException {
    }

    @Test //@Order(1) 
    void platformTransactionManager_shouldBeAvailable() {
        assertTrue(platformTransactionManager.isPresent());
    }
    


}
