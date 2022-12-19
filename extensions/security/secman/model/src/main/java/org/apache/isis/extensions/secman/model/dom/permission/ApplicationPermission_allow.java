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
package org.apache.isis.extensions.secman.model.dom.permission;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_allow.ActionDomainEvent;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = ApplicationPermission_allow.ActionDomainEvent.class,
        associateWith = "rule"
)
@ActionLayout(
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationPermission_allow {

    public static class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationPermission_allow> {}

    private final ApplicationPermission target;

    public ApplicationPermission act() {
        target.setRule(ApplicationPermissionRule.ALLOW);
        return target;
    }

    public String disableAct() {
        return target.getRule() == ApplicationPermissionRule.ALLOW? "Rule is already set to ALLOW": null;
    }
}
