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
package org.apache.isis.core.metamodel.objects;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel._testing.TranslationService_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacetWithStaticTextAbstract;
import org.apache.isis.core.metamodel.id.TypeIdentifierTestFactory;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionDefault;

public class ObjectActionLayoutXmlDefaultTest {

    @Rule public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);


    private ObjectActionDefault action;

    @Mock private FacetedMethod mockFacetedMethod;
    @Mock private InteractionProvider mockInteractionProvider;
    @Mock private SpecificationLoader mockSpecificationLoader;

    protected MetaModelContext metaModelContext;

    @Before
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .interactionProvider(mockInteractionProvider)
                .build();

        context.checking(new Expectations() {
            {
                allowing(mockFacetedMethod).getFeatureIdentifier();
                will(returnValue(Identifier.actionIdentifier(TypeIdentifierTestFactory.newCustomer(), "reduceheadcount")));

                allowing(mockFacetedMethod).getTranslationService();
                will(returnValue(new TranslationService_forTesting()));
            }
        });

        action = ObjectActionDefault.forMethod(mockFacetedMethod);
    }


    @Test
    public void testNameDefaultsToActionsMethodName() {
        final String name = "Reduceheadcount";

        final MemberNamedFacet facet =
                new MemberNamedFacetWithStaticTextAbstract(name, mockFacetedMethod) {};
        context.checking(new Expectations() {
            {
                oneOf(mockFacetedMethod).getFacet(MemberNamedFacet.class);
                will(returnValue(facet));
            }
        });
        assertThat(action.getStaticFriendlyName().get(), is(equalTo(name)));
    }

    @Test
    public void testId() {
        assertEquals("reduceheadcount", action.getId());
    }

}
