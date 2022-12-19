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
package org.apache.isis.metamodel.facets.object.mixin;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.MetaModelContext_forTesting;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodRemoverConstants;
import org.apache.isis.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;

import lombok.val;

abstract class MixinIntendedAs {
    
    protected ProgrammingModelFacetsJava8 programmingModel;
    private MetaModelContext metaModelContext;

    protected void setUp() throws Exception {

        programmingModel = new ProgrammingModelFacetsJava8();
        
        // PRODUCTION

        metaModelContext = MetaModelContext_forTesting.builder()
                .programmingModel(programmingModel)
                .build();
        
        ((ProgrammingModelAbstract)programmingModel)
        .init(new ProgrammingModelInitFilterDefault(), metaModelContext);
        
        metaModelContext.getSpecificationLoader().createMetaModel();
    }

    protected void tearDown() {
        programmingModel = null;
    }

    protected void newContext(
            final Class<?> cls,
            final Method method,
            final int paramNum,
            final MethodRemover methodRemover) {
        
    }

    protected FacetHolder runTypeContextOn(Class<?> type) {
        
        val facetHolder = new AbstractFacetFactoryTest.IdentifiedHolderImpl(
              Identifier.classIdentifier(type));
        facetHolder.setMetaModelContext(metaModelContext);
        
        val processClassContext = 
                new FacetFactory.ProcessClassContext(
                        type, 
                        MethodRemoverConstants.NOOP, 
                        facetHolder);
        
        programmingModel.streamFactories()
//        .filter(facetFactory->!facetFactory.getClass().getSimpleName().startsWith("Grid"))
//        .peek(facetFactory->System.out.println("### " + facetFactory.getClass().getName()))
        .forEach(facetFactory->facetFactory.process(processClassContext));
        
        return facetHolder;
    }
    
    protected FacetedMethodParameter runScalarParameterContextOn(Method actionMethod, int paramIndex) {
        
        val owningType = actionMethod.getDeclaringClass();
        val parameterType = actionMethod.getParameterTypes()[paramIndex];
        
        val facetedMethodParameter = new FacetedMethodParameter(
                FeatureType.ACTION_PARAMETER_SCALAR, 
                owningType, 
                actionMethod, 
                parameterType);
        
        val processParameterContext = 
                new FacetFactory.ProcessParameterContext(
                        owningType, 
                        actionMethod, 
                        paramIndex, 
                        MethodRemoverConstants.NOOP, 
                        facetedMethodParameter);
        
        programmingModel.streamFactories()
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));
        
        return facetedMethodParameter;
    }
    
}
