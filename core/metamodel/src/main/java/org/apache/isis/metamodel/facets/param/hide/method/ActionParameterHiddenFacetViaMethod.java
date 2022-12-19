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

package org.apache.isis.metamodel.facets.param.hide.method;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.facets.param.hide.ActionParameterHiddenFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;

public class ActionParameterHiddenFacetViaMethod extends ActionParameterHiddenFacetAbstract implements ImperativeFacet {

    private final Method method;

    public ActionParameterHiddenFacetViaMethod(
            final Method method,
            final FacetHolder holder) {

        super(holder);
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
        return Intent.CHECK_IF_VALID;
    }

    @Override
    public boolean isHidden(final ManagedObject owningAdapter, final ManagedObject[] argumentAdapters) {
        final Object returnValue = ManagedObject.InvokeUtil.invokeAutofit(method, owningAdapter, argumentAdapters != null ? Arrays.asList(argumentAdapters) : null);
        if(returnValue instanceof Boolean) {
            return (Boolean) returnValue;
        }
        // following precedent for validate, we let this through.
        return false;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        Util.appendAttributesTo(this, attributeMap);
    }

}