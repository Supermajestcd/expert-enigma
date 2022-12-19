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

package org.apache.isis.metamodel.postprocessors.param;

import java.util.List;
import java.util.Map;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;

public class ActionParameterChoicesFacetFromParentedCollection extends ActionParameterChoicesFacetAbstract {

    private final OneToManyAssociation otma;

    public ActionParameterChoicesFacetFromParentedCollection(
            final FacetHolder holder,
            final OneToManyAssociation otma) {
        super(holder);
        this.otma = otma;
    }

    @Override
    public Object[] getChoices(
            final ObjectAdapter target,
            final List<ObjectAdapter> arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final ObjectAdapter parentAdapter = determineParentAdapter(target);
        final ObjectAdapter objectAdapter = otma.get(parentAdapter, interactionInitiatedBy);
        final List<ObjectAdapter> objectAdapters = CollectionFacet.Utils.toAdapterList(objectAdapter);
        return ObjectAdapter.Util.unwrapPojoArray(objectAdapters.toArray(new ObjectAdapter[0]));
    }

    /**
     * in the case of a mixin action, the target passed to the facet is actually the mixin itself, not the mixee.
     */
    private ObjectAdapter determineParentAdapter(final ObjectAdapter target) {
        final MixinFacet mixinFacet = target.getSpecification().getFacet(MixinFacet.class);
        ObjectAdapter mixedInTarget = null;
        if(mixinFacet != null) {
            mixedInTarget = mixinFacet.mixedIn(target, MixinFacet.Policy.FAIL_FAST);
        }
        return mixedInTarget != null ? mixedInTarget : target;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("oneToManyAssociation", otma);
    }
}
