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
package org.apache.isis.extensions.secman.jdo.seed.scripts;

import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.model.app.user.MeService;

/**
 * Role for regular users of the security module, providing the ability to lookup their user account using the
 * {@link org.apache.isis.extensions.secman.model.app.user.MeService}, and for viewing and maintaining their user details.
 */
public class IsisModuleSecurityRegularUserRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    public IsisModuleSecurityRegularUserRoleAndPermissions(SecurityModuleConfig configBean) {
        super(configBean.getRegularUserRoleName(), "Regular user of the security module");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {

        newMemberPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                MeService.class,
                "me");

        newClassPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.VIEWING,
                ApplicationUser.class);

        newMemberPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                ApplicationUser.class,
                "updateName",
                "updatePassword",
                "updateEmailAddress",
                "updatePhoneNumber",
                "updateFaxNumber");

        newMemberPermissions(
                ApplicationPermissionRule.VETO,
                ApplicationPermissionMode.VIEWING,
                ApplicationUser.class,
                "filterPermissions",
                "resetPassword",
                //"updateTenancy", // removed
                "lock", // renamed as 'enable' in the UI
                "unlock", // renamed as 'disable' in the UI
                "addRole",
                "removeRole");

        newMemberPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.VIEWING,
                ApplicationRole.class,
                "name",
                "description");

        //        // for adhoc testing of #42
        //        newMemberPermissions(
        //                ApplicationPermissionRule.ALLOW,
        //                ApplicationPermissionMode.CHANGING,
        //                ApplicationUser.class,
        //                "orphanedUpdateEmailAddress",
        //                "orphanedUpdatePhoneNumber",
        //                "orphanedUpdateFaxNumber");

    }

}
