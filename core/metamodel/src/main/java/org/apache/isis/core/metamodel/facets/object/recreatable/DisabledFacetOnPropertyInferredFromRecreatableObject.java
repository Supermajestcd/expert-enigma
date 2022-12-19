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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class DisabledFacetOnPropertyInferredFromRecreatableObject
extends DisabledFacetAbstract {

    public DisabledFacetOnPropertyInferredFromRecreatableObject(
            final FacetHolder holder,
            final Semantics semantics) {
        super(Where.ANYWHERE,
                "calculated at runtime, based on whether viewmodel is cloneable",
                holder, semantics, Precedence.INFERRED);
    }

    @Override
    public String disabledReason(final ManagedObject target) {
        val viewModelFacet = target.getSpecification().getFacet(ViewModelFacet.class);
        val isCloneable = viewModelFacet.isCloneable(target.getPojo());
        if (!isCloneable) {
            return "Non-cloneable view models are read-only";
        }
        return null;
    }

}
