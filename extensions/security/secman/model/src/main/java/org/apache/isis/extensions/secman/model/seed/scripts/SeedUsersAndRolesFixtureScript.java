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
package org.apache.isis.extensions.secman.model.seed.scripts;

import javax.inject.Inject;

import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.model.seed.SeedSecurityModuleService;
import org.apache.isis.extensions.secman.model.seed.scripts.other.IsisConfigurationRoleAndPermissions;
import org.apache.isis.extensions.secman.model.seed.scripts.other.IsisExtFixturesRoleAndPermissions;
import org.apache.isis.extensions.secman.model.seed.scripts.other.IsisSudoImpersonateRoleAndPermissions;
import org.apache.isis.extensions.secman.model.seed.scripts.other.IsisViewerRestfulObjectsSwaggerRoleAndPermissions;
import org.apache.isis.extensions.secman.model.seed.scripts.secman.GlobalTenancy;
import org.apache.isis.extensions.secman.model.seed.scripts.secman.IsisExtSecmanAdminRoleAndPermissions;
import org.apache.isis.extensions.secman.model.seed.scripts.secman.IsisExtSecmanAdminUser;
import org.apache.isis.extensions.secman.model.seed.scripts.secman.IsisExtSecmanRegularUserRoleAndPermissions;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

/**
 * This fixture script will be run automatically on start-up by virtue of the fact that the
 * {@link SeedSecurityModuleService} is a
 * {@link org.apache.isis.applib.annotation.DomainService} and calls the setup during its
 * {@link SeedSecurityModuleService#onMetamodelEvent(org.apache.isis.core.metamodel.events.MetamodelEvent) init}
 * ({@link javax.annotation.PostConstruct}) method.
 *
 * @since 2.0 {@index}
 */
public class SeedUsersAndRolesFixtureScript extends FixtureScript {

    @Inject private SecmanConfiguration configBean;

    @Override
    protected void execute(ExecutionContext executionContext) {

        // global tenancy
        executionContext.executeChild(this, new GlobalTenancy());

        // secman (admin and regular users)
        executionContext.executeChild(this, new IsisExtSecmanAdminRoleAndPermissions(configBean));
        executionContext.executeChild(this, new IsisExtSecmanAdminUser(configBean));
        executionContext.executeChild(this, new IsisExtSecmanRegularUserRoleAndPermissions(configBean));

        // other modules
        executionContext.executeChild(this, new IsisConfigurationRoleAndPermissions());
        executionContext.executeChild(this, new IsisExtFixturesRoleAndPermissions());
        executionContext.executeChild(this, new IsisSudoImpersonateRoleAndPermissions());
        executionContext.executeChild(this, new IsisViewerRestfulObjectsSwaggerRoleAndPermissions());
    }

}
