/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.datanucleus.identity.DatastoreUniqueLongId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForDatastoreIdImpl;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForDatastoreUniqueLongId;

import lombok.val;

class IdStringifierForDatastoreId_DatastoreUniqueLongId_str_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(0L),
                Arguments.of(10L),
                Arguments.of(Long.MAX_VALUE),
                Arguments.of(Long.MIN_VALUE)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(long value) {

        val entityType = Customer.class;

        String strValue = "" + value;
        val stringifier = new IdStringifierForDatastoreUniqueLongId();

        val stringified = stringifier.enstring(new DatastoreUniqueLongId(strValue));
        val parse = stringifier.destring(stringified, null); // no need to pass entityType

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        // UnsupportedOperationException if attempt this.
        // Assertions.assertThat(parse.getTargetClassName()).isEqualTo(entityType.getName());
    }

}
