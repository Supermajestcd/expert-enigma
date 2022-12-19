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
package org.apache.isis.extensions.secman.jpa.dom.user;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUserManager_newDelegateUser.ActionDomainEvent;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUserManager;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = ActionDomainEvent.class,
        associateWith = "allUsers")
@RequiredArgsConstructor
public class ApplicationUserManager_newDelegateUser
extends org.apache.isis.extensions.secman.model.dom.user.ApplicationUserManager_newDelegateUser<ApplicationRole>{

    private final ApplicationUserManager target;

    @MemberSupport
    public ApplicationUserManager act(

          @Parameter(maxLength = ApplicationUser.Username.MAX_LENGTH)
          @ParameterLayout(named = "Name")
          final String username,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Initial role")
          final ApplicationRole initialRole,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Enabled?")
          final Boolean enabled

            ) {

        super.doAct(username, initialRole, enabled);
        return target;
    }

    @MemberSupport
    public boolean hideAct() {
        return super.doHide();
    }

    @MemberSupport
    public ApplicationRole default1Act() {
        return super.doDefault1();
    }

}
