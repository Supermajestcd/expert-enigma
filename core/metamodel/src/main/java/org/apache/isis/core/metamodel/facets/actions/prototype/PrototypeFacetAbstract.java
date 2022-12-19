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

package org.apache.isis.core.metamodel.facets.actions.prototype;

import java.util.Map;

import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.core.commons.internal.environment.DeploymentType;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;

public abstract class PrototypeFacetAbstract extends FacetAbstract implements PrototypeFacet {

    private final DeploymentType deploymentType;

    public static Class<? extends Facet> type() {
        return PrototypeFacet.class;
    }

    public PrototypeFacetAbstract(final FacetHolder holder, final DeploymentType deploymentType) {
        super(type(), holder);
        this.deploymentType = deploymentType;
    }

    @Override
    public String hides(
            final VisibilityContext<? extends VisibilityEvent> ic) {
        return deploymentType.isProduction()
                ? "Prototyping action not visible in production mode"
                        : null;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("deploymentType", deploymentType.name());
    }
}
