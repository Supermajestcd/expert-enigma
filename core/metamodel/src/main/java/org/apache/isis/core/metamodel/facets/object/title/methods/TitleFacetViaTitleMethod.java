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
import java.util.function.BiConsumer;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TitleFacetViaTitleMethod
extends TitleFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;
    private final TranslationContext translationContext;

    public TitleFacetViaTitleMethod(
            final Method method,
    		final TranslationContext translationContext,
    		final FacetHolder holder) {
        super(holder);
        this.methods = Can.ofSingleton(method);
        this.translationContext = translationContext;
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.UI_HINT;
    }

    @Override
    public String title(final ManagedObject owningAdapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(owningAdapter)) {
            return null;
        }

        val method = methods.getFirstOrFail();
        try {
            final Object returnValue = ManagedObjects.InvokeUtil.invoke(method, owningAdapter);
            if(returnValue instanceof String) {
                return (String) returnValue;
            }
            if(returnValue instanceof TranslatableString) {
                final TranslatableString ts = (TranslatableString) returnValue;
                return ts.translate(getTranslationService(), translationContext);
            }
            return null;
        } catch (final RuntimeException ex) {

            val isUnitTesting = getMetaModelContext().getSystemEnvironment().isUnitTesting();

            if(!isUnitTesting) {
                log.warn("Title failure", ex);
            }
            return "Failed Title";
        }
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
