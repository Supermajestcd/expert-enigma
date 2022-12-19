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
package org.apache.isis.core.metamodel.specloader.validator;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory;

import lombok.val;

@Deprecated //not used any more?
abstract class MetaModelValidatorForDeprecatedAbstract extends MetaModelValidatorAbstract {

    /**
     * @param facet
     */
    public <T extends Facet> T flagIfPresent(T facet) {
        return flagIfPresent(facet, null);
    }

    /**
     * @param facet
     * @param processMethodContext - can be null if none available.
     */
    public <T extends Facet> T flagIfPresent(T facet, FacetFactory.AbstractProcessWithMethodContext<?> processMethodContext) {
        if(facet != null && 
                !getConfiguration().getCore().getMetaModel().getValidator().isAllowDeprecated()) {
            
            val holder = (IdentifiedHolder) facet.getFacetHolder();
            val identifier = holder.getIdentifier();
            super.onFailure(holder, identifier, failureMessageFor(facet, processMethodContext));
        }
        return facet;
    }

    /**
     * Convenience for subclasses.
     */
    static boolean isInherited(FacetFactory.AbstractProcessWithMethodContext<?> processContext) {
        if (processContext == null) {
            return false;
        }
        final Class<?> introspectedCls = processContext.getCls();
        final Class<?> declaringClass = processContext.getMethod().getDeclaringClass();
        return introspectedCls != declaringClass;
    }

    protected abstract String failureMessageFor(Facet facet, FacetFactory.AbstractProcessWithMethodContext<?> processMethodContext);

  

}
