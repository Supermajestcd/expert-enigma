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
package org.apache.isis.metamodel.specloader.validator;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;

import lombok.val;

public class MetaModelValidatorForValidationFailures extends MetaModelValidatorAbstract {

    private final ValidationFailures failures = new ValidationFailures();

    public MetaModelValidatorForValidationFailures() {
    }

    @Override
    public void validate(ValidationFailures validationFailures) {
        validationFailures.addAll(failures);
    }

    public void addFailure(Identifier identifier, String pattern, Object... arguments) {
        failures.add(identifier, pattern, arguments);
    }

    public Facet addFailure(Facet facet, String message) {
        if(facet != null) {
            val holder = (IdentifiedHolder) facet.getFacetHolder();
            val identifier = holder.getIdentifier();
            failures.add(identifier, message + " " + identifier.toFullIdentityString());
        }
        return facet;
    }

    public void addFacet(final Facet facet, final String message) {
        FacetUtil.addFacet(addFailure(facet, message));
    }

}
