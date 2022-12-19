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

package org.apache.isis.core.metamodel.facets.object.cssclass.method;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

public class CssClassFacetMethod
extends FacetAbstract
implements CssClassFacet {

    private static final Class<? extends Facet> type() {
        return CssClassFacet.class;
    }

    private final Method method;


    public CssClassFacetMethod(final Method method, final FacetHolder holder) {
        super(type(), holder);
        this.method = method;
    }

    @Override
    public String cssClass(final ManagedObject owningAdapter) {
        if(owningAdapter == null) {
            return "";
        }
        try {
            return (String) ManagedObjects.InvokeUtil.invoke(method, owningAdapter);
        } catch (final RuntimeException ex) {
            return null;
        }
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("method", method);
    }
}
