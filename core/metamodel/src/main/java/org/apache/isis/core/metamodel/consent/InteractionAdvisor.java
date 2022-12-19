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

package org.apache.isis.core.metamodel.consent;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.InteractionAdvisorFacet;

/**
 * Marker interface for implementations (specifically, {@link Facet}s) that can
 * advise as to whether a member should be disabled.
 *
 * Used within {@link Allow} and {@link Veto}.
 */
public interface InteractionAdvisor {
    
    //boolean handles(InteractionContext ic);

    /**
     * For testing purposes only.
     */
    public static InteractionAdvisor NOOP = new InteractionAdvisorFacet() {
        @Override
        public void appendAttributesTo(final Map<String, Object> attributeMap) {
        }

        @Override
        public boolean alwaysReplace() {
            return false;
        }

        @Override
        public Class<? extends Facet> facetType() {
            return null;
        }

        @Override
        public FacetHolder getFacetHolder() {
            return null;
        }

        @Override
        public boolean isFallback() {
            return true;
        }

        @Override
        public void setFacetHolder(final FacetHolder facetHolder) {
        }

        @Override
        public Facet getUnderlyingFacet() {
            return null;
        }

        @Override
        public void setUnderlyingFacet(final Facet underlyingFacet) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDerived() {
            return false;
        }

        @Override
        public void addContributedFacet(Facet contributedFacet) {
            throw _Exceptions.unsupportedOperation();
        }

        @Override
        public void forEachContributedFacet(Consumer<Facet> onContributedFacet) {
            throw _Exceptions.unsupportedOperation();
        }

        @Override
        public Class<? extends Facet> facetAliasType() {
            return null;
        }

    };

}
