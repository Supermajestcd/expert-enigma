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

package org.apache.isis.core.metamodel.facets.object.title.methods;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;

public class TitleFacetViaTitleMethod extends TitleFacetAbstract implements ImperativeFacet {

    private static final Logger LOG = LoggerFactory.getLogger(TitleFacetViaTitleMethod.class);

    private final Method method;
    private final TranslationService translationService;
    private final String translationContext;

    public TitleFacetViaTitleMethod(final Method method, final TranslationService translationService, final String translationContext, final FacetHolder holder) {
        super(holder);
        this.method = method;
        this.translationService = translationService;
        this.translationContext = translationContext;
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
        return Intent.UI_HINT;
    }

    @Override
    public String title(final ObjectAdapter owningAdapter) {
        try {
            final Object returnValue = ObjectAdapter.InvokeUtils.invoke(method, owningAdapter);
            if(returnValue instanceof String) {
                return (String) returnValue;
            }
            if(returnValue instanceof TranslatableString) {
                final TranslatableString ts = (TranslatableString) returnValue;
                return ts.translate(translationService, translationContext);
            }
            return null;
        } catch (final RuntimeException ex) {
            LOG.warn("title failure", ex);
            return "Failed Title";
        }
    }
}
