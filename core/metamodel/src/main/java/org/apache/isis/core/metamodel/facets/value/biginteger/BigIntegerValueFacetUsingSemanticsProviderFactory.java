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

package org.apache.isis.core.metamodel.facets.value.biginteger;

import java.math.BigInteger;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProviderFactory;

public class BigIntegerValueFacetUsingSemanticsProviderFactory
extends ValueFacetUsingSemanticsProviderFactory<BigInteger> {

    @Inject
    public BigIntegerValueFacetUsingSemanticsProviderFactory(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> type = processClassContext.getCls();
        final FacetHolder holder = processClassContext.getFacetHolder();
        if (type != BigInteger.class) {
            return;
        }
        addFacets(new BigIntegerValueSemanticsProvider(holder));
    }

}
