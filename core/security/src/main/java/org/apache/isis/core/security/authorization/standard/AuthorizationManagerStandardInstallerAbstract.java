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

package org.apache.isis.core.security.authorization.standard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.components.InstallerAbstract;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManagerInstaller;

public abstract class AuthorizationManagerStandardInstallerAbstract
extends InstallerAbstract
implements AuthorizationManagerInstaller {

    public AuthorizationManagerStandardInstallerAbstract(final String name) {
        super(name);
    }

    @Override
    public AuthorizationManager createAuthorizationManager() {
        
        try {
            return createAuthorizationManagerReflective();
            
        } catch (Exception e) {
            
            throw new RuntimeException("unable to create AuthorizationManager reflective", e);
            
        }
    }
    
    //TODO[2040] maybe, there's a way to this more straight forward, without resorting to reflection 
    private AuthorizationManager createAuthorizationManagerReflective() 
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, 
            InvocationTargetException, 
            NoSuchMethodException, SecurityException, InstantiationException {
        
        final String authorizationManagerStandardClsName = 
                "org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard";
        
        final Class<? extends AuthorizationManager> cls = _Casts.uncheckedCast(
                _Context.loadClassAndInitialize(authorizationManagerStandardClsName));
        
        final AuthorizationManager authorizationManager = cls.newInstance();
        final Authorizor authorizor = createAuthorizor();
        
        Method setter = cls.getDeclaredMethod("setAuthorizor", new Class[] {Authorizor.class});
        
        setter.setAccessible(true);
        setter.invoke(authorizationManager, new Object[] {authorizor});
        setter.setAccessible(false);
        
        return authorizationManager;
    }
    
    //TODO[2040] first attempt to decouple modules 'security' and 'metamodel' left us with having
    // to replace this direct code with a reflective one
//    @Override
//    public AuthorizationManager createAuthorizationManager() {
//        final AuthorizationManagerStandard authorizationManager = new AuthorizationManagerStandard();
//        final Authorizor authorizor = createAuthorizor();
//        authorizationManager.setAuthorizor(authorizor);
//        
//        return authorizationManager;
//    }

    /**
     * Hook method
     */
    protected abstract Authorizor createAuthorizor();

    @Override
    public List<Class<?>> getTypes() {
        return listOf(AuthorizationManager.class);
    }

}
