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

package org.apache.isis.core.metamodel.facets.param.choices.enums;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;

import lombok.val;

public class ActionParameterChoicesFacetDerivedFromChoicesFacetFactory extends FacetFactoryAbstract {

    public ActionParameterChoicesFacetDerivedFromChoicesFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        
        val paramType = processParameterContext.getParameterType();

        if(!getSpecificationLoader().loadSpecification(paramType).containsNonFallbackFacet(ChoicesFacet.class)) {
            return;
        }

        // don't trample over any existing facets.
        final FacetedMethodParameter facetHolder = processParameterContext.getFacetHolder();
        if(facetHolder.containsNonFallbackFacet(ActionParameterChoicesFacet.class)) {
            return;
        }

        super.addFacet(new ActionParameterChoicesFacetDerivedFromChoicesFacet(facetHolder));
    }


}
