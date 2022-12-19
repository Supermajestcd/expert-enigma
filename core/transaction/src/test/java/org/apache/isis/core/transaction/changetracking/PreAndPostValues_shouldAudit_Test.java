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
package org.apache.isis.core.transaction.changetracking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.metamodel.services.objectlifecycle.PreAndPostValue;
import org.apache.isis.core.metamodel.services.objectlifecycle.PropertyValuePlaceholder;

import lombok.val;

class PreAndPostValues_shouldAudit_Test {

    @Test
    public void just_created() {
        val preAndPostValue = PreAndPostValue.pre(PropertyValuePlaceholder.NEW)
                .withPost("Foo");

        assertTrue(preAndPostValue.shouldPublish());
    }
    @Test
    public void just_deleted() {
        val preAndPostValue = PreAndPostValue.pre("Foo")
                .withPost(PropertyValuePlaceholder.DELETED);

        assertTrue(preAndPostValue.shouldPublish());
    }
    @Test
    public void changed() {
        val preAndPostValue = PreAndPostValue.pre("Foo")
                .withPost("Bar");

        assertTrue(preAndPostValue.shouldPublish());
    }
    @Test
    public void unchanged() {
        val preAndPostValue = PreAndPostValue.pre("Foo")
                .withPost("Foo");

        assertFalse(preAndPostValue.shouldPublish());
    }
    @Test
    public void created_and_then_deleted() {
        val preAndPostValue = PreAndPostValue.pre(PropertyValuePlaceholder.NEW)
                .withPost(PropertyValuePlaceholder.DELETED);

        assertFalse(preAndPostValue.shouldPublish());
    }
}