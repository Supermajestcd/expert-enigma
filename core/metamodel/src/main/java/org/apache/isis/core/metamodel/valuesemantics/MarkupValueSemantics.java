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

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.Markup;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.MarkupValueSemantics")
public class MarkupValueSemantics
extends ValueSemanticsAbstract<Markup>
implements
    Parser<Markup>,
    Renderer<Markup> {

    @Override
    public Class<Markup> getCorrespondingClass() {
        return Markup.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Markup value) {
        return decomposeAsString(value, Markup::asHtml, ()->null);
    }

    @Override
    public Markup compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, Markup::new, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Markup value) {
        return renderTitle(value, Markup::toString);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Markup adoc) {
        return renderHtml(adoc, Markup::asHtml);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Markup value) {
        return value != null? value.asHtml(): null;
    }

    @Override
    public Markup parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        return text!=null
                ? new Markup(text)
                : null;
    }

    @Override
    public int typicalLength() {
        return 0;
    }

    @Override
    public Can<Markup> getExamples() {
        return Can.of(
                Markup.valueOf("a Markup"),
                Markup.valueOf("another Markup"));
    }


}