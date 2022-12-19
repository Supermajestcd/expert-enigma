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

package org.apache.isis.metamodel.specloader.specimpl;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.metamodel.commons.ToString;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.collparam.semantics.CollectionSemantics;
import org.apache.isis.metamodel.facets.collparam.semantics.CollectionSemanticsFacet;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.interactions.CollectionAddToContext;
import org.apache.isis.metamodel.interactions.CollectionRemoveFromContext;
import org.apache.isis.metamodel.interactions.CollectionUsabilityContext;
import org.apache.isis.metamodel.interactions.CollectionVisibilityContext;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.ValidityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

public class OneToManyAssociationDefault 
extends ObjectAssociationAbstract implements OneToManyAssociation {

    public OneToManyAssociationDefault(final FacetedMethod facetedMethod) {
        this(facetedMethod, facetedMethod.getMetaModelContext()
                .getSpecificationLoader().loadSpecification(facetedMethod.getType()));
    }

    protected OneToManyAssociationDefault(
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec) {

        super(facetedMethod, FeatureType.COLLECTION, objectSpec);
    }

    @Override
    public CollectionSemantics getCollectionSemantics() {
        final CollectionSemanticsFacet facet = getFacet(CollectionSemanticsFacet.class);
        return facet != null ? facet.value() : CollectionSemantics.OTHER_IMPLEMENTATION;
    }


    // -- visible, usable

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new CollectionVisibilityContext(ownerAdapter, getIdentifier(), interactionInitiatedBy, where);
    }


    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new CollectionUsabilityContext(ownerAdapter, getIdentifier(), interactionInitiatedBy, where);
    }



    // -- Validate Add
    // Not API
    private ValidityContext<?> createValidateAddInteractionContext(
            final InteractionInitiatedBy interactionInitiatedBy,
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToAddAdapter) {
        return new CollectionAddToContext(ownerAdapter, getIdentifier(), proposedToAddAdapter,
                interactionInitiatedBy);
    }

    @Override
    public Consent isValidToAdd(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToAddAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return isValidToAddResult(ownerAdapter, proposedToAddAdapter, interactionInitiatedBy).createConsent();
    }

    private InteractionResult isValidToAddResult(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToAddAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ValidityContext<?> validityContext = createValidateAddInteractionContext(
                interactionInitiatedBy, ownerAdapter, proposedToAddAdapter);
        return InteractionUtils.isValidResult(this, validityContext);
    }



    // -- Validate Remove
    private ValidityContext<?> createValidateRemoveInteractionContext(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToRemoveAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new CollectionRemoveFromContext(
                ownerAdapter, getIdentifier(), proposedToRemoveAdapter, interactionInitiatedBy
                );
    }

    @Override
    public Consent isValidToRemove(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToRemoveAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return isValidToRemoveResult(
                ownerAdapter, proposedToRemoveAdapter, interactionInitiatedBy).createConsent();
    }

    private InteractionResult isValidToRemoveResult(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToRemoveAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ValidityContext<?> validityContext = createValidateRemoveInteractionContext(
                ownerAdapter, proposedToRemoveAdapter, interactionInitiatedBy);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    private boolean readWrite() {
        return !isNotPersisted();
    }



    // -- get, isEmpty, add, clear

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final PropertyOrCollectionAccessorFacet accessor = getFacet(PropertyOrCollectionAccessorFacet.class);
        final Object collection = accessor.getProperty(ownerAdapter, interactionInitiatedBy);
        if (collection == null) {
            return null;
        }
        
        val objectManager = super.getObjectManager();
        
        super.getServiceInjector().injectServicesInto(collection);
        
        return objectManager.adapt(collection);
    }

    @Override
    public boolean isEmpty(final ManagedObject parentAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        // REVIEW should we be able to determine if a collection is empty
        // without loading it?
        final ManagedObject collection = get(parentAdapter, interactionInitiatedBy);
        final CollectionFacet facet = CollectionFacet.Utils.getCollectionFacetFromSpec(collection);
        return facet.size(collection) == 0;
    }

    // -- add, clear

    @Override
    public void addElement(
            final ManagedObject ownerAdapter,
            final ManagedObject referencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        if (referencedAdapter == null) {
            throw new IllegalArgumentException("Can't use null to add an item to a collection");
        }
        if (readWrite()) {
            
            ManagedObject._whenFirstIsBookmarkable_ensureSecondIsNotTransient(
                    ownerAdapter, 
                    referencedAdapter);
                    
            val facet = getFacet(CollectionAddToFacet.class);
            facet.add(ownerAdapter, referencedAdapter, interactionInitiatedBy);
        }
    }

    @Override
    public void removeElement(
            final ManagedObject ownerAdapter,
            final ManagedObject referencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if (referencedAdapter == null) {
            throw new IllegalArgumentException("element should not be null");
        }
        if (readWrite()) {
            final CollectionRemoveFromFacet facet = getFacet(CollectionRemoveFromFacet.class);
            facet.remove(ownerAdapter, referencedAdapter, interactionInitiatedBy);
        }
    }

    public void removeAllAssociations(final ManagedObject ownerAdapter) {
        final CollectionClearFacet facet = getFacet(CollectionClearFacet.class);
        facet.clear(ownerAdapter);
    }

    // -- defaults
    @Override
    public ManagedObject getDefault(final ManagedObject ownerAdapter) {
        return null;
    }

    @Override
    public void toDefault(final ManagedObject ownerAdapter) {
    }


    // -- choices & autoComplete

    @Override
    public ManagedObject[] getChoices(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new ManagedObject[0];
    }

    @Override
    public boolean hasChoices() {
        return false;
    }


    @Override
    public boolean hasAutoComplete() {
        return false;
    }

    @Override
    public ManagedObject[] getAutoComplete(
            ManagedObject object,
            String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new ManagedObject[0];
    }

    @Override
    public int getAutoCompleteMinLength() {
        return 0; // n/a
    }



    // -- toString

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.append(",");
        str.append("persisted", !isNotPersisted());
        str.append("type", getSpecification() == null ? "unknown" : getSpecification().getShortIdentifier());
        return str.toString();
    }




}
