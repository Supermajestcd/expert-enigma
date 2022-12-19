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
package org.apache.isis.testdomain.auditing;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.audit.AuditerService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class AuditerServiceForTesting implements AuditerService {

    @Inject private KVStoreForTesting kvStore;
    
    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void audit(UUID interactionId, int sequence, String targetClassName, Bookmark target,
            String memberIdentifier, String propertyName, String preValue, String postValue, String user,
            Timestamp timestamp) {

        val audit = new StringBuilder()
                .append("targetClassName=").append(targetClassName).append(",").append("propertyName=")
                .append(propertyName).append(",").append("preValue=").append(preValue).append(",")
                .append("postValue=").append(postValue).append(";")
                .toString();

        kvStore.put(this, "audit", audit);
        log.debug("audit {}", audit);
    }

}