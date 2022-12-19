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

package org.apache.isis.extensions.sse.facets;

import org.apache.isis.extensions.sse.api.ServerSentEvents;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;

import lombok.val;

public class SseAnnotationFacetFactory extends FacetFactoryAbstract {

    public SseAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        processObserve(processMethodContext);
    }


    void processObserve(final ProcessMethodContext processMethodContext) {
        val facetHolder = processMethodContext.getFacetHolder();

        val serverSentEventsIfAny = processMethodContext.synthesizeOnMethod(ServerSentEvents.class);
        val facet = SseObserveFacetForServerSentEventsAnnotation.create(serverSentEventsIfAny, facetHolder);

        FacetUtil.addFacet(facet);
    }


}
