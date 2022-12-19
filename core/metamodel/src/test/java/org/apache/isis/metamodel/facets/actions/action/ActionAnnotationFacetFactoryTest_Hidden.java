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
package org.apache.isis.metamodel.facets.actions.action;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ActionAnnotationFacetFactoryTest_Hidden extends ActionAnnotationFacetFactoryTest {

    @Test
    public void withAnnotation() {

        class Customer {
            @Action(hidden = Where.REFERENCES_PARENT)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
                cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processHidden(processMethodContext);

        // then
        final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
        Assert.assertNotNull(hiddenFacet);
        assertThat(hiddenFacet.where(), is(Where.REFERENCES_PARENT));

        final Facet hiddenFacetImpl = facetedMethod.getFacet(HiddenFacetForActionAnnotation.class);
        Assert.assertNotNull(hiddenFacetImpl);
        Assert.assertTrue(hiddenFacet == hiddenFacetImpl);
    }

}