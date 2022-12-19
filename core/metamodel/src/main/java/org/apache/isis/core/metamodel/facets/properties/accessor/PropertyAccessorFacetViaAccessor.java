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

package org.apache.isis.core.metamodel.facets.properties.accessor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class PropertyAccessorFacetViaAccessor
        extends PropertyOrCollectionAccessorFacetAbstract
        implements ImperativeFacet {


    private final Method method;

    public PropertyAccessorFacetViaAccessor(
            final ObjectSpecification typeSpec,
            final Method method,
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final IsisConfiguration isisConfiguration,
            final SpecificationLoader specificationLoader,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final AdapterManager adapterManager) {
        super(typeSpec, holder, deploymentCategory, isisConfiguration, specificationLoader, authenticationSessionProvider, adapterManager);
        this.method = method;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.ACCESSOR;
    }

    @Override
    public Object getProperty(
            final ObjectAdapter owningAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final Object referencedObject = ObjectAdapter.InvokeUtils.invoke(method, owningAdapter);

        if(referencedObject == null) {
            return null;
        }

        boolean filterForVisibility = getConfiguration().getBoolean("isis.reflector.facet.filterVisibility", true);
        if(filterForVisibility) {
            final ObjectAdapter referencedAdapter = getAdapterManager().adapterFor(referencedObject);
            final boolean visible = ObjectAdapter.Util
                    .isVisible(referencedAdapter, interactionInitiatedBy);
            if (!visible) {
                return null;
            }
        }
        return referencedObject;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

}
