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

package org.apache.isis.security.authentication.manager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authorization.manager.AuthorizationManager;
import org.apache.isis.security.authorization.standard.Authorizor;

@Singleton
public class AuthorizationManagerStandard implements AuthorizationManager {


    // /////////////////////////////////////////////////////////
    // init, shutddown
    // /////////////////////////////////////////////////////////

    @PostConstruct
    @Override
    public void init() {
        authorizor.init();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        authorizor.shutdown();
    }

    // /////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////

    @Override
    public boolean isUsable(final AuthenticationSession session, final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }
        if(containsSudoSuperuserRole(session)) {
            return true;
        }
        if (authorizor.isUsableInAnyRole(identifier)) {
            return true;
        }
        
        if(session.streamRoles()
                .anyMatch(roleName->authorizor.isUsableInRole(roleName, identifier)) ) {
            return true;
        }
        
        return false;
    }

    @Override
    public boolean isVisible(final AuthenticationSession session, final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }

        // no-op if is visibility context check at object-level
        if (identifier.getMemberName().equals("")) {
            return true;
        }

        if(containsSudoSuperuserRole(session)) {
            return true;
        }
        if (authorizor.isVisibleInAnyRole(identifier)) {
            return true;
        }
        if(session.streamRoles()
                .anyMatch(roleName->authorizor.isVisibleInRole(roleName, identifier)) ) {
            return true;
        }
        return false;
    }

    private static boolean containsSudoSuperuserRole(final AuthenticationSession session) {
        return session.hasRole(SudoService.ACCESS_ALL_ROLE);
    }

    private boolean isPerspectiveMember(final Identifier identifier) {
        return (identifier.getClassName().equals(""));
    }

//[2112]    
//    @EventListener(ProgrammingModel.class)
//    public static void refineProgrammingModel(@Observes ProgrammingModel baseProgrammingModel) {
//        final AuthorizationFacetFactory facetFactory = new AuthorizationFacetFactory();
//        baseProgrammingModel.addFactory(facetFactory);
//    }

    @Inject protected Authorizor authorizor;

}
