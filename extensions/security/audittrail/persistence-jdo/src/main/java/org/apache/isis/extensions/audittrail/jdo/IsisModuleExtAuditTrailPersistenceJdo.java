/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.audittrail.jdo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.audittrail.applib.IsisModuleExtAuditTrailApplib;
import org.apache.isis.extensions.audittrail.jdo.dom.AuditTrailEntry;
import org.apache.isis.extensions.audittrail.jdo.dom.AuditTrailEntryRepository;
import org.apache.isis.persistence.jdo.datanucleus.IsisModulePersistenceJdoDatanucleus;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;


@Configuration
@Import({
        // modules
        IsisModuleTestingFixturesApplib.class,
        IsisModuleExtAuditTrailApplib.class,
        IsisModulePersistenceJdoDatanucleus.class,

        // services
        AuditTrailEntryRepository.class,

        // entities, eager meta-model introspection
        AuditTrailEntry.class,
})
public class IsisModuleExtAuditTrailPersistenceJdo {

    /**
     * Note that this is <i>NOT</i> an implementation of the {@link ModuleWithFixtures#getTeardownFixture()} API;
     * but is provided to allow manual teardown if required.
     */
    public FixtureScript teardownFixture() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(AuditTrailEntry.class);
            }
        };
    }

}