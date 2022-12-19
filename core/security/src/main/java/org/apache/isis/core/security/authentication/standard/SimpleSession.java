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

package org.apache.isis.core.security.authentication.standard;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.isis.core.security.authentication.AuthenticationSessionAbstract;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.Getter;
import lombok.Setter;

public class SimpleSession extends AuthenticationSessionAbstract {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_VALIDATION_CODE = "";

    /**
     * Defaults {@link #getValidationCode()} to empty string (<tt>""</tt>).
     */
    public SimpleSession(final String userName, final Iterable<String> roles) {
        this(userName, stream(roles), DEFAULT_VALIDATION_CODE);
    }

    public SimpleSession(final String userName, final Collection<String> roles, final String code) {
        this(userName, roles.stream(), code);
    }

    public SimpleSession(final String userName, final Stream<String> roles, final String code) {
        super(userName, roles, code);
    }

    @Getter @Setter
    private Type type = Type.DEFAULT;


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SimpleSession other = (SimpleSession) obj;
        return equals(other);
    }

    public boolean equals(final SimpleSession other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return getUserName().equals(other.getUserName());
    }

    @Override
    public int hashCode() {
        return getUserName().hashCode();
    }

}
