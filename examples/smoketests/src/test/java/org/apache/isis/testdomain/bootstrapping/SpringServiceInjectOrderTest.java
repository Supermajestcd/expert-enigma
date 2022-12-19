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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.config.IsisPresets;
import org.apache.isis.runtime.services.message.MessageServiceDefault;
//import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Getter;
import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                SpringServiceInjectOrderTest.TestConfig.class,
                
                SpringServiceInjectOrderTest.Average.class,
                SpringServiceInjectOrderTest.Excellent.class,
                SpringServiceInjectOrderTest.Good.class,
                SpringServiceInjectOrderTest.DummyService.class
        },
        properties = {
                "logging.config=log4j2-test.xml",
                // "isis.reflector.introspector.parallelize=false",
                // "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
        })
@TestPropertySource({
    IsisPresets.DebugDiscovery
})
//@Incubating("with development work on 'v2' the reference list of services constantly changes")
class SpringServiceInjectOrderTest {

    @Configuration
    static class TestConfig {
// is now in IsisBoot
//        @Bean
//        public OrderComparator orderComparator() {
//            return new AnnotationAwareOrderComparator();
//        }
    }
    
    interface Rating {
        int getRating();
    }
    
    @Service
    @Order(1)
    static class Excellent implements Rating {
     
        @Override
        public int getRating() {
            return 1;
        }
    }
     
    @Service
    @Order(2) @Primary
    static class Good implements Rating {
     
        @Override
        public int getRating() {
            return 2;
        }
    }
     
    @Service
    @Order(Ordered.LOWEST_PRECEDENCE)
    static class Average implements Rating {
     
        @Override
        public int getRating() {
            return 3;
        }
    }
    
    @Service
    static class DummyService {
        @Inject @Getter MessageService messageService;
        @Inject @Getter List<Rating> ratings;
        @Inject @Getter Rating primaryRating;
    }
    
    @DomainObject
    static class DummyObject {
        @Inject @Getter MessageService messageService;
        @Inject @Getter List<Rating> ratings;
        @Inject @Getter Rating primaryRating;
    }
    
    
    @Inject DummyService dummyService;
    @Inject ServiceInjector serviceInjector;
    @Inject OrderComparator orderComparator;
    
    @BeforeEach
    void beforeEach() {

    }

    @Test
    void defaultOrdering_shouldConsiderAnnotations() throws IOException {
        assertTrue(orderComparator instanceof AnnotationAwareOrderComparator);
    }
    
    @Test
    void injectionOnServices_shouldFollowOrder() throws IOException {

        val ratings = dummyService.getRatings();
        val primaryRating = dummyService.getPrimaryRating();
        
        assertThat(ratings.get(0).getRating(), is(equalTo(1)));
        assertThat(ratings.get(1).getRating(), is(equalTo(2)));
        assertThat(ratings.get(2).getRating(), is(equalTo(3)));
        
        assertThat(primaryRating.getRating(), is(equalTo(2)));
        
        val messageService = dummyService.getMessageService();
        assertNotNull(messageService);
        assertTrue(messageService instanceof MessageServiceDefault);
    }
    
    @Test
    void injectionOnObjects_shouldFollowOrder() throws IOException {

        val dummyObject = new DummyObject();
        serviceInjector.injectServicesInto(dummyObject);
        
        val ratings = dummyObject.getRatings();
        val primaryRating = dummyObject.getPrimaryRating();
        
        assertThat(ratings.get(0).getRating(), is(equalTo(1)));
        assertThat(ratings.get(1).getRating(), is(equalTo(2)));
        assertThat(ratings.get(2).getRating(), is(equalTo(3)));
        
        assertThat(primaryRating.getRating(), is(equalTo(2)));
        
        val messageService = dummyObject.getMessageService();
        assertNotNull(messageService);
        assertTrue(messageService instanceof MessageServiceDefault);
    }

}
