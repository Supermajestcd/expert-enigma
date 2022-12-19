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

package org.apache.isis.core.metamodel.facets.actcoll.typeof;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacet;

import lombok.val;

/**
 * The type of the collection or the action.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * collection's accessor or the action's invoker method with the
 * {@link Collection#typeOf} annotation.
 */
public interface TypeOfFacet extends SingleClassValueFacet {

    // -- FACTORIES

    static TypeOfFacet inferredFromArray(
            final FacetHolder holder,
            final Class<?> elementType) {
        return new TypeOfFacetInferredFromArray(elementType, holder);
    }

    static TypeOfFacet inferredFromGenerics(
            final FacetHolder holder,
            final Class<?> elementType) {
        return new TypeOfFacetInferredFromGenerics(elementType, holder);
    }

    static Optional<TypeOfFacet> inferFromParameterType(
            final FacetHolder holder,
            final Parameter param) {

        val paramType = param.getType();

        if (_Arrays.isArrayType(paramType)) {
            return _Arrays.inferComponentType(paramType)
                    .map(elementType->inferredFromArray(holder, elementType));
        }

        return _Collections.inferElementType(param)
                .map(elementType->inferredFromGenerics(holder, elementType));
    }

    static Optional<TypeOfFacet> inferFromMethodReturnType(
            final FacetHolder holder,
            final Method method) {

        val returnType = method.getReturnType();

        if (_Arrays.isArrayType(returnType)) {
            return _Arrays.inferComponentType(returnType)
                    .map(elementType->inferredFromArray(holder, elementType));
        }

        return _Collections.inferElementType(method)
                .map(elementType->inferredFromGenerics(holder, elementType));
    }


}
