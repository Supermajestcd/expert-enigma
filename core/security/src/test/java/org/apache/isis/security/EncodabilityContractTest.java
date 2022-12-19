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
package org.apache.isis.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.standard.SimpleAuthentication;

import lombok.SneakyThrows;
import lombok.val;

public abstract class EncodabilityContractTest {

    protected Authentication simpleAuthSession;
    protected Serializable serializable;

    public EncodabilityContractTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        serializable = createEncodable();
        simpleAuthSession = SimpleAuthentication.validOf(UserMemento.ofName("test"));
    }

    /**
     * Hook for subclasses to provide object to be tested.
     */
    protected abstract Serializable createEncodable();

    @Test
    public void shouldImplementEncodeable() throws Exception {
        assertThat(serializable, is(instanceOf(Serializable.class)));
    }

    @Test
    public void shouldRoundTrip() throws IOException, ClassNotFoundException {
        val decodedObject = doRoundTrip(serializable);
        assertRoundtripped(decodedObject, serializable);
    }
    
    @SneakyThrows
    private static <T extends Serializable> T doRoundTrip(T serializable) {
        
        val buffer = new ByteArrayOutputStream();
        
        try(val out = new ObjectOutputStream(buffer)) {
            out.writeObject(serializable);
        }
        
        try(val in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            val decodedObject = in.readObject();
            return _Casts.uncheckedCast(decodedObject);
        }
    }

    protected abstract void assertRoundtripped(Object decodedEncodable, Object originalEncodable);

}