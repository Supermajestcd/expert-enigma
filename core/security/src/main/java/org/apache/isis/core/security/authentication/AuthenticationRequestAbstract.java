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

package org.apache.isis.core.security.authentication;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Sets;

public abstract class AuthenticationRequestAbstract implements AuthenticationRequest {

    private final String name;
    private final Set<String> roles = _Sets.newHashSet();

    public AuthenticationRequestAbstract(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stream<String> streamRoles() {
        return roles.stream();
    }

    @Override
    public void addRole(String role) {
        if(_Strings.isNullOrEmpty(role)) {
            return; // ignore
        }
        this.roles.add(role);
    }

}
