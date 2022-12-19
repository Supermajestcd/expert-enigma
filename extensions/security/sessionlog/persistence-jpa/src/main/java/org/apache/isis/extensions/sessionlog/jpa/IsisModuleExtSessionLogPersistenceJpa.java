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

package org.apache.isis.extensions.sessionlog.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.persistence.jpa.eclipselink.IsisModulePersistenceJpaEclipselink;
import org.apache.isis.extensions.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.extensions.sessionlog.jpa.dom.SessionLogEntry;
import org.apache.isis.extensions.sessionlog.jpa.dom.SessionLogEntryPK;
import org.apache.isis.extensions.sessionlog.jpa.dom.SessionLogEntryRepository;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jpa.TeardownFixtureJpaAbstract;


@Configuration
@Import({
        // modules
        IsisModuleTestingFixturesApplib.class,
        IsisModuleExtSessionLogApplib.class,
        IsisModulePersistenceJpaEclipselink.class,

        // services
        SessionLogEntryRepository.class,
        SessionLogEntryPK.Stringifier.class,

        // entities, eager meta-model introspection
        SessionLogEntry.class,
})
@EntityScan(basePackageClasses = {
        SessionLogEntry.class,
})
public class IsisModuleExtSessionLogPersistenceJpa implements ModuleWithFixtures {

    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureJpaAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(SessionLogEntry.class);
            }
        };
    }

}
