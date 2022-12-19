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

import java.util.Objects;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.metamodel.interactions.AccessContext;
import org.apache.isis.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.metamodel.interactions.InteractionContext;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.applib.util.schema.CommandDtoUtils;

import lombok.val;

public abstract class ObjectMemberAbstract 
implements ObjectMember, MetaModelContext.Delegating, FacetHolder.Delegating {

    protected ObjectSpecification specificationOf(final Class<?> type) {
        return type != null 
                ? getMetaModelContext().getSpecificationLoader().loadSpecification(type)
                        : null;
    }

    // -- fields
    private final String id;
    private final FacetedMethod facetedMethod;
    private final FeatureType featureType;

    protected ObjectMemberAbstract(
            final FacetedMethod facetedMethod,
            final FeatureType featureType) {

        final String id = facetedMethod.getIdentifier().getMemberName();
        if (id == null) {
            throw new IllegalArgumentException("Id must always be set");
        }
        this.facetedMethod = facetedMethod;
        this.featureType = featureType;
        this.id = id;
    }

    // -- Identifiers

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Identifier getIdentifier() {
        return getFacetedMethod().getIdentifier();
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    // -- Facets

    /**
     * Not API.
     */
    public FacetedMethod getFacetedMethod() {
        return facetedMethod;
    }

    // -- Name, Description, Help (convenience for facets)
    /**
     * Return the default label for this member. This is based on the name of
     * this member.
     *
     * @see #getId()
     */
    @Override
    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        final String name = facet.value();
        if (name != null) {
            return name;
        }
        else {
            // this should now be redundant, see NamedFacetDefault
            return StringExtensions.asNaturalName2(getId());
        }
    }

    @Override
    public String getDescription() {
        final DescribedAsFacet facet = getFacet(DescribedAsFacet.class);
        return facet.value();
    }

    @Override
    public String getHelp() {
        final HelpFacet facet = getFacet(HelpFacet.class);
        return facet.value();
    }



    // -- Hidden (or visible)
    /**
     * Create an {@link InteractionContext} to represent an attempt to view this
     * member (that is, to check if it is visible or not).
     *
     * <p>
     * Typically it is easier to just call
     * {@link ObjectMember#isVisible(ManagedObject, InteractionInitiatedBy, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    protected abstract VisibilityContext<?> createVisibleInteractionContext(
            final ManagedObject targetObjectAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);



    @Override
    public boolean isAlwaysHidden() {
        final HiddenFacet facet = getFacet(HiddenFacet.class);
        return facet != null &&
                !facet.isFallback() &&
                (facet.where() == Where.EVERYWHERE || facet.where() == Where.ANYWHERE)
                ;

    }

    /**
     * Loops over all {@link HidingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none hide the member.
     */
    @Override
    public Consent isVisible(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return isVisibleResult(target, interactionInitiatedBy, where).createConsent();
    }

    private InteractionResult isVisibleResult(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        final VisibilityContext<?> ic = createVisibleInteractionContext(target, interactionInitiatedBy, where);
        return InteractionUtils.isVisibleResult(this, ic);
    }


    // -- Disabled (or enabled)
    /**
     * Create an {@link InteractionContext} to represent an attempt to
     * use this member (that is, to check if it is usable or not).
     *
     * <p>
     * Typically it is easier to just call
     * {@link ObjectMember#isUsable(ManagedObject, InteractionInitiatedBy, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    protected abstract UsabilityContext<?> createUsableInteractionContext(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);

    /**
     * Loops over all {@link DisablingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none disables the member.
     */
    @Override
    public Consent isUsable(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return isUsableResult(target, interactionInitiatedBy, where).createConsent();
    }

    private InteractionResult isUsableResult(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        final UsabilityContext<?> ic = createUsableInteractionContext(target, interactionInitiatedBy, where);
        return InteractionUtils.isUsableResult(this, ic);
    }



    // -- isAssociation, isAction
    @Override
    public boolean isAction() {
        return featureType.isAction();
    }

    @Override
    public boolean isPropertyOrCollection() {
        return featureType.isPropertyOrCollection();
    }

    @Override
    public boolean isOneToManyAssociation() {
        return featureType.isCollection();
    }

    @Override
    public boolean isOneToOneAssociation() {
        return featureType.isProperty();
    }


    // -- mixinAdapterFor
    /**
     * For mixins
     */
    ManagedObject mixinAdapterFor(
            final Class<?> mixinType,
            final ManagedObject mixedInAdapter) {
        
        val spec = getSpecificationLoader().loadSpecification(mixinType);
        val mixinFacet = spec.getFacet(MixinFacet.class);
        val mixinPojo = mixinFacet.instantiate(mixedInAdapter.getPojo());
        return ManagedObject.of(spec, mixinPojo);
    }

    static String determineNameFrom(final ObjectAction mixinAction) {
        return StringExtensions.asCapitalizedName(suffix(mixinAction));
    }

    static String determineIdFrom(final ObjectActionDefault mixinAction) {
        final String id = StringExtensions.asCamelLowerFirst(compress(suffix(mixinAction)));
        return id;
    }

    private static String compress(final String suffix) {
        return suffix.replaceAll(" ","");
    }

    private static String suffix(final ObjectAction mixinAction) {
        return deriveMemberNameFrom(mixinAction.getOnType().getSingularName());
    }

    public static String deriveMemberNameFrom(final String mixinClassName) {
        final String deriveFromUnderscore = derive(mixinClassName, "_");
        if(!Objects.equals(mixinClassName, deriveFromUnderscore)) {
            return deriveFromUnderscore;
        }
        final String deriveFromDollar = derive(mixinClassName, "$");
        if(!Objects.equals(mixinClassName, deriveFromDollar)) {
            return deriveFromDollar;
        }
        return mixinClassName;
    }

    private static String derive(final String singularName, final String separator) {
        final int indexOfSeparator = singularName.lastIndexOf(separator);
        return occursNotAtEnd(singularName, indexOfSeparator)
                ? singularName.substring(indexOfSeparator + 1)
                        : singularName;
    }

    private static boolean occursNotAtEnd(final String singularName, final int indexOfUnderscore) {
        return indexOfUnderscore != -1 && indexOfUnderscore != singularName.length() - 1;
    }



    // -- toString

    @Override
    public String toString() {
        return String.format("id=%s,name='%s'", getId(), getName());
    }

    // -- Dependencies

    protected CommandContext getCommandContext() {
        return getServiceRegistry().lookupServiceElseFail(CommandContext.class);
    }

    protected CommandDtoServiceInternal getCommandDtoService() {
        return getServiceRegistry().lookupServiceElseFail(CommandDtoServiceInternal.class);
    }

    // -- command (setup)


    protected void setupCommandTarget(final ManagedObject targetAdapter, final String arguments) {
        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        if(command.getTarget() != null) {
            // is set up by the outer-most action; inner actions (invoked via the WrapperFactory) must not overwrite
            return;
        }

        command.internal().setTargetClass(CommandUtil.targetClassNameFor(targetAdapter));
        command.internal().setTargetAction(CommandUtil.targetMemberNameFor(this));
        command.internal().setArguments(arguments);

        final Bookmark targetBookmark = CommandUtil.bookmarkFor(targetAdapter);
        command.internal().setTarget(targetBookmark);
    }

    protected void setupCommandMemberIdentifier() {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        if (command.getMemberIdentifier() != null) {
            // any contributed/mixin actions will fire after the main action
            // the guard here prevents them from trashing the command's memberIdentifier
            return;
        }

        command.internal().setMemberIdentifier(CommandUtil.memberIdentifierFor(this));
    }

    protected void setupCommandDtoAndExecutionContext(final CommandDto dto) {
        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        if (command.getMemento() != null) {
            // guard here to prevent subsequent contributed/mixin actions from
            // trampling over the command's memento and execution context
            return;
        }

        // memento

        final String mementoXml = CommandDtoUtils.toXml(dto);
        command.internal().setMemento(mementoXml);

        // copy over the command execution 'context' (if available)
        final CommandFacet commandFacet = getFacetHolder().getFacet(CommandFacet.class);
        if(commandFacet != null && !commandFacet.isDisabled()) {
            //command.internal().setExecuteIn(commandFacet.executeIn());
            command.internal().setPersistence(commandFacet.persistence());
        } else {
            // if no facet, assume do want to execute right now, but only persist (eventually) if hinted.
            //command.internal().setExecuteIn(org.apache.isis.applib.annotation.CommandExecuteIn.FOREGROUND);
            command.internal().setPersistence(org.apache.isis.applib.annotation.CommandPersistence.IF_HINTED);
        }
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return facetedMethod.getMetaModelContext();
    }

}