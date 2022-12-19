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

package org.apache.isis.core.metamodel.facets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public interface FacetFactory {

    static class AbstractProcessContext<T extends FacetHolder> {
        private final T facetHolder;

        public AbstractProcessContext(final T facetHolder) {
            this.facetHolder = facetHolder;
        }

        public T getFacetHolder() {
            return facetHolder;
        }
    }

    static class AbstractProcessWithClsContext<T extends FacetHolder>
    extends AbstractProcessContext<T>{

        private final Class<?> cls;

        AbstractProcessWithClsContext(final Class<?> cls, final T facetHolder) {
            super(facetHolder);
            this.cls = cls;
        }

        /**
         * The class being introspected upon.
         */
        public Class<?> getCls() {
            return cls;
        }
        
        /** 
         * Annotation lookup on this context's type (cls).
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnType(Class<A> annotationType) {
            return _Annotations.synthesizeInherited(cls, annotationType);
        }
        
    }

    static class AbstractProcessWithMethodContext<T extends FacetHolder> 
    extends AbstractProcessWithClsContext<T> implements MethodRemover{

        private final Method method;
        protected final MethodRemover methodRemover;

        AbstractProcessWithMethodContext(
                final Class<?> cls, 
                final Method method, 
                final MethodRemover methodRemover, 
                final T facetHolder) {
            
            super(cls, facetHolder);
            this.method = method;
            this.methodRemover = methodRemover;
        }

        /**
         * The class being introspected upon.
         *
         * <p>
         *     This isn't necessarily the same as the {@link java.lang.reflect.Method#getDeclaringClass() declaring class} of the {@link #getMethod() method}; the method might have been inherited.
         * </p>
         */
        @Override
        public Class<?> getCls() {
            return super.getCls();
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public void removeMethod(final Method method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(Predicate<Method> filter, Consumer<Method> onRemoval) {
            methodRemover.removeMethods(filter, onRemoval);
        }

    }

    public interface ProcessContextWithMetadataProperties<T extends FacetHolder> {
        public T getFacetHolder();
    }

    /**
     * The {@link FeatureType feature type}s that this facet factory can create
     * {@link Facet}s for.
     *
     * <p>
     * Used by the Java5 Reflector's <tt>ProgrammingModel</tt> to reduce the
     * number of {@link FacetFactory factory}s that are queried when building up
     * the meta-model.
     * 
     */
    ImmutableEnumSet<FeatureType> getFeatureTypes();


    // //////////////////////////////////////
    // process class
    // //////////////////////////////////////

    public static class ProcessClassContext 
    extends AbstractProcessWithClsContext<FacetHolder> 
    implements MethodRemover, ProcessContextWithMetadataProperties<FacetHolder> {
        
        private final MethodRemover methodRemover;

        /**
         * For testing only.
         */
        public ProcessClassContext(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder facetHolder) {
            super(cls, facetHolder);
            this.methodRemover = methodRemover;
        }


        @Override
        public void removeMethod(final Method method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(Predicate<Method> filter, Consumer<Method> onRemoval) {
            methodRemover.removeMethods(filter, onRemoval);
        }

    }

    /**
     * Process the class, and return the correctly setup annotation if present.
     */
    void process(ProcessClassContext processClassContext);

    // //////////////////////////////////////
    // process method
    // //////////////////////////////////////


    public static class ProcessMethodContext 
    extends AbstractProcessWithMethodContext<FacetedMethod> 
    implements ProcessContextWithMetadataProperties<FacetedMethod> {
        
        @Getter private final FeatureType featureType;
        /**
         * Whether we are currently processing a mixin type AND this context's method can be identified 
         * as the main method of the processed mixin class. 
         * @since 2.0
         */
        @Getter private final boolean mixinMain;

        /**
         * 
         * @param cls
         * @param featureType
         * @param method
         * @param methodRemover
         * @param facetedMethod
         * @param isMixinMain
         *       - Whether we are currently processing a mixin type AND this context's method can be identified 
         *         as the main method of the processed mixin class. (since 2.0)
         */
        public ProcessMethodContext(
                final Class<?> cls,
                final FeatureType featureType,
                final Method method,
                final MethodRemover methodRemover,
                final FacetedMethod facetedMethod,
                final boolean isMixinMain) {
            
            super(cls, method, methodRemover, facetedMethod);
            this.featureType = featureType;
            this.mixinMain = isMixinMain;
        }
        
        /** JUnit support, historically not using 'isMixinMain' */
        public ProcessMethodContext(
                final Class<?> cls,
                final FeatureType featureType,
                final Method method,
                final MethodRemover methodRemover,
                final FacetedMethod facetedMethod) {
            this(cls, featureType, method, methodRemover, facetedMethod, false);
        }

        
        /** 
         * Annotation lookup on this context's method. Also honors annotations on fields, if this method is a getter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnMethod(Class<A> annotationType) {
            return _Annotations.synthesizeInherited(getMethod(), annotationType);
        }
        
        /** 
         * Annotation lookup on this context's method, if not found, extends search to type in case 
         * the predicate {@link #isMixinMain} evaluates {@code true}.
         * <p>
         * As of [ISIS-2604] we also make sure the annotation type does not appear in both places 
         * (method and type). Hence the 2nd parameter is a callback that fires if the annotation 
         * is found in both places. 
         * 
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnMethodOrMixinType(
                final @NonNull Class<A> annotationType,
                final @NonNull Runnable onAmbiguity) {
            
            
            val onMethod = synthesizeOnMethod(annotationType);
            val onType = synthesizeOnType(annotationType);
            
            if(onMethod.isPresent()) {
                if(onType.isPresent()) {
                    onAmbiguity.run();    
                }
                return onMethod;
            }
            return onType;
        }
        
    }
    
    
    /**
     * Process the method, and return the correctly setup annotation if present.
     */
    void process(ProcessMethodContext processMethodContext);

    // -- PROCESS PARAM

    public static class ProcessParameterContext 
    extends AbstractProcessWithMethodContext<FacetedMethodParameter> {
        
        private final int paramNum;
        private final Class<?> paramType;
        private final Parameter parameter; 

        public ProcessParameterContext(
                final Class<?> cls,
                final Method method,
                final int paramNum,
                final MethodRemover methodRemover,
                final FacetedMethodParameter facetedMethodParameter) {
            
            super(cls, method, methodRemover, facetedMethodParameter);
            if(paramNum>=method.getParameterCount()) {
                throw _Exceptions.unrecoverable("invalid ProcessParameterContext");
            }
            this.paramNum = paramNum;
            this.paramType = super.method.getParameterTypes()[paramNum];
            this.parameter = super.method.getParameters()[paramNum];
        }

        public int getParamNum() {
            return paramNum;
        }
        
        /** 
         * Annotation lookup on this context's method parameter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnParameter(Class<A> annotationType) {
            return _Annotations.synthesizeInherited(parameter, annotationType);
        }

        /**
         * @since 2.0
         */
        public Class<?> getParameterType() {
            return this.paramType;
        }
        
        /**
         * @since 2.0
         */
        public Parameter getParameter() {
            return parameter;
        }
    }

    /**
     * Process the parameters of the method, and return the correctly setup
     * annotation if present.
     */
    void processParams(ProcessParameterContext processParameterContext);

    
}
