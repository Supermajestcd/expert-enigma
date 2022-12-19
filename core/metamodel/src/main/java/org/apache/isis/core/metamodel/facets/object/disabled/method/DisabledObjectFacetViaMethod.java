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

package org.apache.isis.core.metamodel.facets.object.disabled.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.Identifier.Type;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.disabled.DisabledObjectFacetAbstract;

public class DisabledObjectFacetViaMethod extends DisabledObjectFacetAbstract implements ImperativeFacet {

    private final Method method;
    private TranslationService translationService;
    private final String translationContext;

    public DisabledObjectFacetViaMethod(
            final Method method,
            final TranslationService translationService,
            final String translationContext,
            final FacetHolder holder) {
        super(holder);
        this.method = method;
        this.translationService = translationService;
        this.translationContext = translationContext;
    }

    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHECK_IF_DISABLED;
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    @Override
    public String disabledReason(final ObjectAdapter owningAdapter, final Identifier identifier) {
        final Type type = identifier.getType();
        final Object returnValue = ObjectAdapter.InvokeUtils.invoke(method, owningAdapter, type);
        if(returnValue instanceof String) {
            return (String) returnValue;
        }
        if(returnValue instanceof TranslatableString) {
            final TranslatableString ts = (TranslatableString) returnValue;
            return ts.translate(translationService, translationContext);
        }
        return null;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

    @Override
    public void copyOnto(final FacetHolder holder) {
        final DisabledObjectFacetViaMethod clonedFacet = new DisabledObjectFacetViaMethod(this.method, translationService, translationContext, holder);
        FacetUtil.addFacet(clonedFacet);
    }
}