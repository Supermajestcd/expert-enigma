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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.val;

public class OneToOneAssociationContributee 
extends OneToOneAssociationDefault 
implements ContributeeMember {

    private final ManagedBeanAdapter serviceBean;
    private final ObjectAction serviceAction;

    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder = new FacetHolderImpl();

    private final Identifier identifier;

    public OneToOneAssociationContributee(
            final ManagedBeanAdapter serviceBean,
            final ObjectActionDefault serviceAction,
            final ObjectSpecification contributeeType) {

        super(serviceAction.getFacetedMethod(), serviceAction.getReturnType());

        this.serviceBean = serviceBean;

        this.serviceAction = serviceAction;

        //
        // ensure the contributed property cannot be modified
        //
        final NotPersistedFacet notPersistedFacet = new NotPersistedFacetAbstract(this) {};
        final DisabledFacet disabledFacet = disabledFacet();

        FacetUtil.addFacet(notPersistedFacet);
        FacetUtil.addFacet(disabledFacet);

        //
        // in addition, copy over facets from contributed to own.
        //
        // These could include everything under @Property(...) because the
        // PropertyAnnotationFacetFactory is also run against actions.
        //
        final FacetedMethod contributor = serviceAction.getFacetedMethod();
        FacetUtil.copyFacets(contributor, facetHolder);

        // calculate the identifier
        final Identifier contributorIdentifier = contributor.getIdentifier();
        final String memberName = contributorIdentifier.getMemberName();
        List<String> memberParameterNames = contributorIdentifier.getMemberParameterNames();

        identifier = Identifier.actionIdentifier(contributeeType.getCorrespondingClass().getName(), memberName, memberParameterNames);
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null &&
                originalFacet.where() == Where.ANYWHERE) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee("Contributed property", this);
    }

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter, 
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val params = Collections.singletonList(ownerAdapter);
        
        return serviceAction.execute(getServiceAdapter(), /*mixin*/null, params, interactionInitiatedBy);
    }

    @Override
    public ObjectSpecification getOnType() {
        return serviceAction.getOnType();
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isContributedBy(final ObjectAction serviceAction) {
        return serviceAction == this.serviceAction;
    }

    @Override
    public int getContributeeParamIndex() {
        // always 0 for contributed properties
        return 0;
    }

    @Override
    public Consent isVisible(
            final ManagedObject contributee,
            final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        final VisibilityContext<?> ic = ((ObjectMemberAbstract)serviceAction).createVisibleInteractionContext(
                getServiceAdapter(), interactionInitiatedBy, where);
        ic.putContributee(0, contributee); // by definition, the contributee will be the first arg of the service action
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ManagedObject contributee,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where) {
        final UsabilityContext<?> ic = ((ObjectMemberAbstract)serviceAction).createUsableInteractionContext(
                getServiceAdapter(), interactionInitiatedBy, where);
        ic.putContributee(0, contributee); // by definition, the contributee will be the first arg of the service action
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    private ManagedObject getServiceAdapter() {
        return getObjectManager().adapt(serviceBean);
    }

    // -- Contributee impl - getServiceContributedBy()
    @Override
    public ObjectSpecification getServiceContributedBy() {
        return getServiceAdapter().getSpecification();
    }



}
