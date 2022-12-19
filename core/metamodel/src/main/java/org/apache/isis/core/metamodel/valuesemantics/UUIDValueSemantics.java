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
package org.apache.isis.core.metamodel.valuesemantics;

import java.util.UUID;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.UUIDValueSemantics")
public class UUIDValueSemantics
extends ValueSemanticsAbstract<UUID>
implements
    Parser<UUID>,
    Renderer<UUID> {

    @Override
    public Class<UUID> getCorrespondingClass() {
        return UUID.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final UUID value) {
        return decomposeAsString(value, UUID::toString, ()->null);
    }

    @Override
    public UUID compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, UUID::fromString, ()->null);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final UUID value) {
        return value == null ? "" : value.toString();
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final UUID value) {
        return value == null ? null : value.toString();
    }

    @Override
    public UUID parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? UUID.fromString(input)
                : null;
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 36;
    }

    @Override
    public Can<UUID> getExamples() {
        return Can.of(
                UUID.fromString("57f2b7fa-1aed-41af-b9a2-8a25824335ac"),
                UUID.fromString("d2302a34-a393-498c-977a-10ea6a6cb422"),
                UUID.fromString("a89ba9b7-5e59-447d-bae7-5d91e2daa85b"));
    }

    //    private static final Pattern pattern = Pattern.compile(
    //            "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");

}
