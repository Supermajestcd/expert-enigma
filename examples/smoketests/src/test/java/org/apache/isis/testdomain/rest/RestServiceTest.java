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
package org.apache.isis.testdomain.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.viewer.restfulobjects.IsisBootWebRestfulObjects;

import lombok.val;

@SpringBootTest(
        classes = {RestService.class},
        properties = {
                "logging.config=log4j2-test.xml",
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    Configuration_usingJdo.class,
    IsisBootWebRestfulObjects.class
})
class RestServiceTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestService restService;

    @Test
    void bookOfTheWeek_viaRestEndpoint() {

        assertNotNull(restService.getPort());
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getRecommendedBookOfTheWeek(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val bookOfTheWeek = digest.get();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());

    }
    
    @Test
    void httpSessionInfo() {

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        val digest = restService.getHttpSessionInfo(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val httpSessionInfo = digest.get();

        assertNotNull(httpSessionInfo);
        assertEquals("no http-session", httpSessionInfo);

    }
    
    

}
