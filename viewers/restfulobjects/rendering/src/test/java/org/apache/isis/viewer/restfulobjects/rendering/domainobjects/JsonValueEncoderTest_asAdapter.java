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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;
import org.apache.isis.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderServiceDefault;

import lombok.val;

public class JsonValueEncoderTest_asAdapter {

    @Rule public JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock private ObjectSpecification mockObjectSpec;
    @Mock private ValueFacet mockValueFacet;
    @Mock private ManagedObject mockObjectAdapter;
    @Mock private SpecificationLoader specLoader;

    private JsonRepresentation representation;
    private JsonValueEncoderService jsonValueEncoder;

    @Before
    public void setUp() throws Exception {

        jsonValueEncoder = JsonValueEncoderServiceDefault.forTesting(specLoader);

        representation = new JsonRepresentation(TextNode.valueOf("aString"));
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test(expected = IllegalArgumentException.class)
    public void whenSpecIsNull() throws Exception {
        jsonValueEncoder.asAdapter(null, representation, null);
    }

    @Test
    public void whenReprIsNull() throws Exception {
        assertThat(jsonValueEncoder.asAdapter(mockObjectSpec, null, null), is(Matchers.nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsAnArray() throws Exception {
        allowingObjectSpecHas(ValueFacet.class, mockValueFacet);
        jsonValueEncoder.asAdapter(mockObjectSpec, JsonRepresentation.newArray(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReprIsAMap() throws Exception {
        allowingObjectSpecHas(ValueFacet.class, mockValueFacet);
        assertNull(jsonValueEncoder.asAdapter(mockObjectSpec, JsonRepresentation.newMap(), null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSpecDoesNotHaveAnEncodableFacet() throws Exception {
        allowingObjectSpecHas(ValueFacet.class, null);

        assertNull(jsonValueEncoder.asAdapter(mockObjectSpec, representation, null));
    }

    @Test
    public void whenReprIsBooleanPrimitive() throws Exception {
        whenReprIsBoolean(boolean.class);
    }

    @Test
    public void whenReprIsBooleanWrapper() throws Exception {
        whenReprIsBoolean(Boolean.class);
    }

    private void whenReprIsBoolean(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHasValue(correspondingClass);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(correspondingClass);
        final boolean value = true;
        representation = new JsonRepresentation(BooleanNode.valueOf(value));
        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(((Object)value).getClass());
                will(returnValue(Optional.of(mockObjectSpec)));
            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBooleanButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHasValue(boolean.class);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(boolean.class);

        context.checking(new Expectations() {
            {
                allowing(mockValueFacet).fromEncodedString(Format.JSON, "aString");
                will(throwException(new TextEntryParseException("'aString' cannot be parsed as a boolean value")));
            }
        });

        // when
        jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsIntegerPrimitive() throws Exception {
        whenReprIsInteger(int.class);
    }

    @Test
    public void whenReprIsIntegerWrapper() throws Exception {
        whenReprIsInteger(Integer.class);
    }

    private void whenReprIsInteger(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHasValue(correspondingClass);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(correspondingClass);
        final int value = 123;
        representation = new JsonRepresentation(IntNode.valueOf(value));
        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(((Object)value).getClass());
                will(returnValue(Optional.of(mockObjectSpec)));
            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsIntegerButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(ValueFacet.class, mockValueFacet);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(int.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsLongPrimitive() throws Exception {
        whenReprIsLong(long.class);
    }

    @Test
    public void whenReprIsLongWrapper() throws Exception {
        whenReprIsLong(Long.class);
    }

    private void whenReprIsLong(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHasValue(correspondingClass);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(correspondingClass);
        final long value = 1234567890L;
        representation = new JsonRepresentation(LongNode.valueOf(value));
        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(((Object)value).getClass());
                will(returnValue(Optional.of(mockObjectSpec)));
            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsLongButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHasValue(long.class);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(long.class);

        context.checking(new Expectations() {
            {
                allowing(mockValueFacet).fromEncodedString(Format.JSON, "aString");
                will(throwException(new TextEntryParseException("'aString' cannot be parsed as a long value")));
            }
        });

        // when
        jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsDoublePrimitive() throws Exception {
        whenReprIsDouble(double.class);
    }

    @Test
    public void whenReprIsDoubleWrapper() throws Exception {
        whenReprIsDouble(Double.class);
    }

    private void whenReprIsDouble(final Class<?> correspondingClass) {
        // given
        allowingObjectSpecHasValue(correspondingClass);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(correspondingClass);
        final double value = 123.45;
        representation = new JsonRepresentation(DoubleNode.valueOf(value));
        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(((Object)value).getClass());
                will(returnValue(Optional.of(mockObjectSpec)));
            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsDoubleButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(ValueFacet.class, mockValueFacet);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(double.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsBigInteger() throws Exception {
        // given
        allowingObjectSpecHasValue(BigInteger.class);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(BigInteger.class);
        final BigInteger value = BigInteger.valueOf(123);
        representation = new JsonRepresentation(BigIntegerNode.valueOf(value));
        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(value.getClass());
                will(returnValue(Optional.of(mockObjectSpec)));
            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBigIntegerButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(ValueFacet.class, mockValueFacet);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(BigInteger.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsBigDecimal() throws Exception {
        // given
        allowingObjectSpecHasValue(BigDecimal.class);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(BigDecimal.class);
        final BigDecimal value = new BigDecimal("123234234.45612312343535");
        representation = new JsonRepresentation(DecimalNode.valueOf(value));
        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(value.getClass());
                will(returnValue(Optional.of(mockObjectSpec)));

            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenObjectSpecIsBigDecimalButReprIsNot() throws Exception {
        // given
        allowingObjectSpecHas(ValueFacet.class, mockValueFacet);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(BigDecimal.class);

        representation = JsonRepresentation.newMap("foo", "bar");

        // when
        jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);
    }

    @Test
    public void whenReprIsString() throws Exception {
        // given
        allowingObjectSpecHasValue(String.class);
        allowingObjectSpecCorrespondingClassAndObjectTypeIs(String.class);
        representation = new JsonRepresentation(TextNode.valueOf("aString"));

        context.checking(new Expectations() {
            {
                allowing(specLoader).specForType(String.class);
                will(returnValue(Optional.of(mockObjectSpec)));
            }
        });

        // when
        val adapter = jsonValueEncoder.asAdapter(mockObjectSpec, representation, null);

        // then
        assertSame(mockObjectSpec, adapter.getSpecification());
    }



    private void allowingObjectSpecHasValue(final Class<?> valueClass) {
        context.checking(new Expectations() {
            {

                allowing(mockObjectSpec).valueFacet();
                will(returnValue(Optional.of(mockValueFacet)));

                allowing(mockObjectSpec).getFacet(ValueFacet.class);
                will(returnValue(mockValueFacet));

                allowing(mockObjectSpec).lookupFacet(ValueFacet.class);
                will(returnValue(Optional.of(mockValueFacet)));

                allowing(mockValueFacet).getValueClass();
                will(returnValue(valueClass));

                allowing(mockObjectSpec).isNonScalar();
                will(returnValue(true));

            }
        });
    }

    private <T extends Facet> void allowingObjectSpecHas(final Class<T> facetClass, final T facet) {
        context.checking(new Expectations() {
            {

                allowing(mockObjectSpec).valueFacet();
                will(returnValue(Optional.ofNullable(facet)));

                allowing(mockObjectSpec).getFacet(facetClass);
                will(returnValue(facet));

                allowing(mockObjectSpec).lookupFacet(facetClass);
                will(returnValue(Optional.ofNullable(facet)));

                allowing(mockObjectSpec).getCorrespondingClass();
                will(returnValue(mockObjectSpec.getClass())); // used only for illegal argument exception message

            }
        });
    }

    private void allowingObjectSpecCorrespondingClassAndObjectTypeIs(final Class<?> result) {
        context.checking(new Expectations() {
            {
                allowing(mockObjectSpec).getCorrespondingClass();
                will(returnValue(result));

                allowing(mockObjectSpec).getLogicalType();
                will(returnValue(LogicalType.fqcn(result)));

            }
        });
    }

}
