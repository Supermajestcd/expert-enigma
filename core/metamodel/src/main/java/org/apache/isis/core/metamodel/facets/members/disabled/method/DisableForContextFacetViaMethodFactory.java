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

package org.apache.isis.core.metamodel.facets.members.disabled.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

public class DisableForContextFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final Can<String> PREFIXES = Can.ofSingleton(MethodLiteralConstants.DISABLE_PREFIX);

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public DisableForContextFacetViaMethodFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachDisabledFacetIfDisabledMethodIsFound(processMethodContext);
    }

    private void attachDisabledFacetIfDisabledMethodIsFound(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(method.getName());

        final Class<?> cls = processMethodContext.getCls();

        Method disableMethod = null;

        boolean noParamsOnly = getConfiguration().getCore().getMetaModel().getValidator().isNoParamsOnly();
        boolean searchExactMatch = !noParamsOnly;
        if(searchExactMatch) {
            // search for exact match
            disableMethod = MethodFinderUtils.findMethod_returningText(
                    cls,
                    MethodLiteralConstants.DISABLE_PREFIX + capitalizedName,
                    method.getParameterTypes());
        }
        if (disableMethod == null) {
            // search for no-arg version
            disableMethod = MethodFinderUtils.findMethod_returningText(
                    cls,
                    MethodLiteralConstants.DISABLE_PREFIX + capitalizedName,
                    new Class<?>[0]);
        }
        if (disableMethod == null) {
            return;
        }

        processMethodContext.removeMethod(disableMethod);

        final FacetHolder facetHolder = processMethodContext.getFacetHolder();
        final TranslationService translationService = getTranslationService();
        // sadness: same logic as in I18nFacetFactory
        final String translationContext = ((IdentifiedHolder)facetHolder).getIdentifier().toClassAndNameIdentityString();
        super.addFacet(new DisableForContextFacetViaMethod(disableMethod, translationService, translationContext, facetHolder));
    }

}
