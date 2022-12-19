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
package org.apache.isis.testdomain.persistence.jpa;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;

import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_usingJpa.class,
        })
@TestPropertySources({
    @TestPropertySource(IsisPresets.UseLog4j2Test)    
})
//@Transactional ... we manage transaction ourselves
class JpaExceptionTranslationTest
{

    // @Inject private JpaSupportService jpaSupport;
    
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repositoryService;
    @Inject private InteractionFactory interactionFactory;
    @Inject private JpaTransactionManager txManager;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test 
    void booksUniqueByIsbn_whenViolated_shouldThrowTranslatedException() {

        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            
            interactionFactory.runAnonymous(()->{
            
                _TestFixtures.setUp3Books(repositoryService);
                
            });
            
            
        });
        
        // when adding a book for which one with same ISBN already exists in the database,
        // we expect to see a Spring recognized DataAccessException been thrown 
        
        assertThrows(DataAccessException.class, ()->{
        
            transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
                
                interactionFactory.runAnonymous(()->{
                
                    // given
                    
                    val inventories = repositoryService.allInstances(JpaInventory.class);
                    assertEquals(1, inventories.size());
                    
                    val inventory = inventories.get(0);
                    assertNotNull(inventory);
                    
                    
                    // add a conflicting book (unique ISBN violation)
                    _TestFixtures.addABookTo(inventory);
                
                });
    
            })
            .ifSuccess(__->fail("expected to fail, but did not"))
            //.mapFailure(ex->_JpaExceptionTranslator.translate(ex, txManager)) 
            .ifFailure(ex->assertTrue(ex instanceof DataIntegrityViolationException))
            .optionalElseFail();
           
        });
        
        // expected post condition: ONE inventory with 3 books
        
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            
            interactionFactory.runAnonymous(()->{
            
                val inventories = repositoryService.allInstances(JpaInventory.class);
                assertEquals(1, inventories.size());
                
                val inventory = inventories.get(0);
                assertNotNull(inventory);
                
                assertNotNull(inventory);
                assertNotNull(inventory.getProducts());
                assertEquals(3, inventory.getProducts().size());

                _TestFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);
                
            });
            
            
        });

        
    }
    
}