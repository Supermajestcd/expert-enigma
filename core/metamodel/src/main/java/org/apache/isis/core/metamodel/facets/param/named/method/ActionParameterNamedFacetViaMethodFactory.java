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

package org.apache.isis.core.metamodel.facets.param.named.method;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

public class ActionParameterNamedFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = {"named"};

    public ActionParameterNamedFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
        final List<FacetedMethodParameter> holderList = facetedMethod.getParameters();

        attachNamedFacetForParametersIfAutoCompleteNumMethodIsFound(processMethodContext, holderList);

    }

    private void attachNamedFacetForParametersIfAutoCompleteNumMethodIsFound(
            final ProcessMethodContext processMethodContext,
            final List<FacetedMethodParameter> parameters) {

        if (parameters.isEmpty()) {
            return;
        }

        final Method actionMethod = processMethodContext.getMethod();
        final Class<?>[] params = actionMethod.getParameterTypes();

        for (int i = 0; i < params.length; i++) {

            final Class<?> paramType = params[i];

            final Class[] returnTypes = { String.class };
            Method namedMethod = findNamedNumMethodReturning(processMethodContext, i, returnTypes);
            if (namedMethod == null) {
                continue;
            }
            processMethodContext.removeMethod(namedMethod);

            // add facets directly to parameters, not to actions
            final FacetedMethodParameter paramAsHolder = parameters.get(i);
            FacetUtil.addFacet(
                    new NamedFacetOnParameterMethod(
                            namedMethod, paramType,
                            getDeploymentCategory(), getSpecificationLoader(),
                            getAuthenticationSessionProvider(), adapterManager));
        }
    }

    private Method findNamedNumMethodReturning(
            final ProcessMethodContext processMethodContext,
            final int paramNum,
            final Class<?>[] returnTypes) {

        final Class<?> cls = processMethodContext.getCls();
        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        final String name = MethodPrefixConstants.AUTO_COMPLETE_PREFIX + paramNum + capitalizedName;
        return MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, name, returnTypes, new Class[]{String.class});
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        adapterManager = servicesInjector.getPersistenceSessionServiceInternal();
    }

    PersistenceSessionServiceInternal adapterManager;

}
