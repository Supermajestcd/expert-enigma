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
package org.apache.isis.commons.collections;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal.testing._SerializationTester;

import lombok.val;

class CanTest {

    @Test
    void tester_selftest() throws ClassNotFoundException, IOException {
        _SerializationTester.selftest();
    }
    
    @Test
    void emptyCans_shouldBeEqual() {
        assertEquals(Can.empty(), Can.<String>of());
    }
    
    @Test
    void emptyCan_shouldBeSerializable() {
        _SerializationTester.assertEqualsOnRoundtrip(Can.empty());
        _SerializationTester.assertEqualsOnRoundtrip(Can.<String>of());
    }
    
    @Test
    void singletonCan_shouldBeSerializable() {
        _SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi"));
    }
    
    @Test
    void multiCan_shouldBeSerializable() {
        _SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi", "there"));
    }
    
    // -- REVERTING
    
    @Test
    void multiCan_correctly_reverts() {
        assertEquals(Can.<String>of("c", "b", "a"), Can.<String>of("a", "b", "c").reverse());
    }
    
    @Test
    void multiCan_startsWith() {
        assertTrue(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b", "c")));
        assertFalse(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b", "c", "x")));
        assertTrue(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b")));
        assertTrue(Can.<String>of("a", "b", "c").startsWith(Can.<String>empty()));
        assertTrue(Can.<String>of("a", "b", "c").startsWith(null));
        assertFalse(Can.<String>of("a", "b", "c").startsWith(Can.<String>of("a", "b", "x")));
    }
    
    @Test
    void multiCan_endsWith() {
        assertTrue(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("a", "b", "c")));
        assertFalse(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("x", "a", "b", "c")));
        assertTrue(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("b", "c")));
        assertTrue(Can.<String>of("a", "b", "c").endsWith(Can.<String>empty()));
        assertTrue(Can.<String>of("a", "b", "c").endsWith(null));
        assertFalse(Can.<String>of("a", "b", "c").endsWith(Can.<String>of("x", "b", "a")));
    }
    
    // -- FILTERING
    
    @Test
    void emptyCanFilter_isIdentity() {
        assertEquals(Can.<String>empty(), Can.<String>empty().filter(x->false));
        assertEquals(Can.<String>empty(), Can.<String>empty().filter(null));
    }
    
    @Test
    void singletonCanFilter_whenAccept_isIdentity() {
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi").filter(x->true));
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi").filter(null));
    }
    
    @Test
    void canFilter_whenNotAccept_isEmpty() {
        assertEquals(Can.<String>empty(), Can.<String>of("hi").filter(x->false));
        assertEquals(Can.<String>empty(), Can.<String>of("hi", "there").filter(x->false));
    }
    
    @Test
    void multiCanFilter_whenAccept_isIdentity() {
        assertEquals(Can.<String>of("hi", "there"), Can.<String>of("hi", "there").filter(x->true));
        assertEquals(Can.<String>of("hi", "there"), Can.<String>of("hi", "there").filter(null));
    }
    
    @Test
    void multiCanFilter_whenAcceptOne_isDifferentCan() {
        assertEquals(Can.<String>of("there"), Can.<String>of("hi", "there").filter("there"::equals));
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi", "there").filter("hi"::equals));
        assertEquals(Can.<String>of("hello"), Can.<String>of("hi", "hello", "there").filter("hello"::equals));
    }
    
    @Test
    void multiCanFilter_whenAcceptTwo_isDifferentCan() {
        assertEquals(Can.<String>of("hi", "hello"), Can.<String>of("hi", "hello", "there").filter(x->x.startsWith("h")));
    }
    
    // -- STREAM IDIOMS
    
    @Test
    void partialSums_reversed() {
        assertEquals(Can.<String>of("a", "b"), Can.<String>empty().add("a").add("b"));
        assertEquals(Can.<String>of("a", "b"), Can.<String>empty().add(0, "b").add(0, "a"));
        
        final Can<String> all = Can.<String>of("a", "b", "c");
        
        val iterator = all.reverseIterator();
        
        val partialSums = Stream.iterate(
                Can.<String>empty(), 
                parts->parts.add(0, iterator.next()))
        .limit(4)
        .collect(Can.toCan());
        
        assertEquals(Can.of(
                    Can.<String>empty(),
                    Can.<String>of("c"),
                    Can.<String>of("b", "c"),
                    Can.<String>of("a", "b", "c")
                ), 
                partialSums);
        
    }
    
    
    
    
    

}