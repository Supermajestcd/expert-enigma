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
package org.apache.isis.extensions.secman.model.dom.user;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.UpdateNameDomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = UpdateNameDomainEvent.class, 
        associateWith = "knownAs")
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class ApplicationUser_updateName {
    
    private final ApplicationUser target;

    @MemberSupport
    public ApplicationUser act(
            @Parameter(maxLength = ApplicationUser.MAX_LENGTH_FAMILY_NAME, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Family Name")
            final String familyName,
            @Parameter(maxLength = ApplicationUser.MAX_LENGTH_GIVEN_NAME, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Given Name")
            final String givenName,
            @Parameter(maxLength = ApplicationUser.MAX_LENGTH_KNOWN_AS, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Known As")
            final String knownAs
            ) {
        target.setFamilyName(familyName);
        target.setGivenName(givenName);
        target.setKnownAs(knownAs);
        return target;
    }

    @MemberSupport
    public String default0Act() {
        return target.getFamilyName();
    }

    @MemberSupport
    public String default1Act() {
        return target.getGivenName();
    }

    @MemberSupport
    public String default2Act() {
        return target.getKnownAs();
    }

    @MemberSupport
    public String disableAct() {
        return target.isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }

    @MemberSupport
    public String validateAct(final String familyName, final String givenName, final String knownAs) {
        if(familyName != null && givenName == null) {
            return "Must provide given name if family name has been provided.";
        }
        if(familyName == null && (givenName != null | knownAs != null)) {
            return "Must provide family name if given name or 'known as' name has been provided.";
        }
        return null;
    }
}
