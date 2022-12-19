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

package org.apache.isis.viewer.wicket.model.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.routing.RoutingService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModel;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.action.form.FormPendingParamUiModel;
import org.apache.isis.viewer.common.model.action.form.FormUiModel;
import org.apache.isis.viewer.common.model.mementos.ActionMemento;

import lombok.val;

public final class ActionModel 
extends ManagedObjectModel 
implements FormUiModel, FormExecutorContext, BookmarkableModel {

    private static final long serialVersionUID = 1L;

    public ActionModel copy() {
        return new ActionModel(this);
    }

    // -- FACTORY METHODS

    public static ActionModel of(EntityModel actionOwner, ObjectAction action) {
        return of(actionOwner, new ActionMemento(action));
    }
    
    public static ActionModel of(EntityModel actionOwner, ActionMemento actionMemento) {
        return new ActionModel(actionOwner, actionMemento);
    }

    public static ActionModel ofPageParameters(
            IsisAppCommonContext commonContext, 
            PageParameters pageParameters) {
        
        return PageParameterUtil.actionModelFor(commonContext, pageParameters);
    }
  

    // -- BOOKMARKABLE

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        val adapter = getOwner();
        val objectAction = getMetaModel();
        return PageParameterUtil.createPageParametersForAction(adapter, objectAction, argCache().snapshot());
    }

    @Override
    public PageParameters getPageParameters() {
        return getPageParametersWithoutUiHints();
    }

    // --
    
    private transient ObjectAction objectAction;
    @Override
    public ObjectAction getMetaModel() {
        if(objectAction==null) {
            objectAction = actionMemento.getAction(this::getSpecificationLoader); 
        }
        return objectAction;
    }

    @Override
    public boolean hasAsRootPolicy() {
        return true;
    }
    
    @Override
    public EntityModel getParentUiModel() {
        return ownerModel;
    }

    // -- HELPERS

    private final EntityModel ownerModel;
    private final ActionMemento actionMemento;

    // lazy in support of serialization of this class
    private transient ActionArgumentCache argCache;
    private ActionArgumentCache argCache() {
        return argCache!=null
                ? argCache
                : (argCache = createActionArgumentCache());
    }
    private ActionArgumentCache createActionArgumentCache() {
        return new ActionArgumentCache(
                ownerModel, 
                actionMemento, 
                getMetaModel());
    }

    private ActionModel(EntityModel entityModel, ActionMemento actionMemento) {
        super(entityModel.getCommonContext());
        this.ownerModel = entityModel;
        this.actionMemento = actionMemento;
    }

    /**
     * Copy constructor, as called by {@link #copy()}.
     */
    private ActionModel(ActionModel actionModel) {
        super(actionModel.getCommonContext());
        this.ownerModel = actionModel.ownerModel;
        this.actionMemento = actionModel.actionMemento;
        this.argCache = actionModel.argCache().copy(); 
    }

    @Override
    public ManagedObject getOwner() {
        return ownerModel.load();
    }

    @Override
    protected ManagedObject load() {

        // from getObject()/reExecute
        detach(); // force re-execute

        // TODO: think we need another field to determine if args have been populated.
        val results = executeAction();

        return results;
    }

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where
    public static final Where WHERE_FOR_ACTION_INVOCATION = Where.ANYWHERE;

    private ManagedObject executeAction() {

        val targetAdapter = getOwner();
        final Can<ManagedObject> arguments = argCache().snapshot();
        final ObjectAction action = getMetaModel();
        
        val head = action.interactionHead(targetAdapter);
        
        val resultAdapter =
                action.executeWithRuleChecking(
                        head, arguments,
                        InteractionInitiatedBy.USER,
                        WHERE_FOR_ACTION_INVOCATION);

        val resultPojo = resultAdapter != null ? resultAdapter.getPojo() : null;

        return getServiceRegistry()
                .select(RoutingService.class)
                .stream()
                .filter(routingService->routingService.canRoute(resultPojo))
                .map(routingService->routingService.route(resultPojo))
                .filter(_NullSafe::isPresent)
                .map(super.getObjectManager()::adapt)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(resultAdapter);

    }

    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("target adapter for ActionModel cannot be changed");
    }

    public PendingParameterModel getArgumentsAsParamModel() {
        return getMetaModel().interactionHead(getOwner())
                .model(argCache().snapshot());
    }


    /** Resets arguments to their fixed point default values
     * @see {@link PendingParameterModelHead#defaults()}
     */
    public void clearArguments() {

        val defaultsFixedPoint = getMetaModel()
                .interactionHead(getOwner())
                .defaults()
                .getParamValues();

        argCache().resetTo(defaultsFixedPoint);
    }

    /**
     * Bookmarkable if the {@link ObjectAction action} has a {@link BookmarkPolicyFacet bookmark} policy
     * of {@link BookmarkPolicy#AS_ROOT root}, and has safe {@link ObjectAction#getSemantics() semantics}.
     */
    public boolean isBookmarkable() {
        final ObjectAction action = getMetaModel();
        final BookmarkPolicyFacet bookmarkPolicy = action.getFacet(BookmarkPolicyFacet.class);
        final boolean safeSemantics = action.getSemantics().isSafeInNature();
        return bookmarkPolicy.value() == BookmarkPolicy.AS_ROOT && safeSemantics;
    }

    // //////////////////////////////////////

    /**
     * Simply executes the action.
     *
     * Previously there was exception handling code here also, but this has now been centralized
     * within FormExecutorAbstract
     */
    public ManagedObject execute() {
        final ManagedObject resultAdapter = this.getObject();
        return resultAdapter;
    }


    // //////////////////////////////////////

    public static IRequestHandler redirectHandler(final Object value) {
        if(value instanceof java.net.URL) {
            final java.net.URL url = (java.net.URL) value;
            return new RedirectRequestHandler(url.toString());
        }
        if(value instanceof LocalResourcePath) {
            final LocalResourcePath localResourcePath = (LocalResourcePath) value;
            return new RedirectRequestHandler(localResourcePath.getPath());
        }
        return null;
    }

    public static IRequestHandler downloadHandler(final Object value) {
        if(value instanceof Clob) {
            final Clob clob = (Clob)value;
            return handlerFor(resourceStreamFor(clob), clob);
        }
        if(value instanceof Blob) {
            final Blob blob = (Blob)value;
            return handlerFor(resourceStreamFor(blob), blob);
        }
        return null;
    }

    private static IResourceStream resourceStreamFor(final Blob blob) {
        final IResourceStream resourceStream = new AbstractResourceStream() {

            private static final long serialVersionUID = 1L;

            @Override
            public InputStream getInputStream() throws ResourceStreamNotFoundException {
                return new ByteArrayInputStream(blob.getBytes());
            }

            @Override
            public String getContentType() {
                return blob.getMimeType().toString();
            }

            @Override
            public void close() throws IOException {
            }
        };
        return resourceStream;
    }

    private static IResourceStream resourceStreamFor(final Clob clob) {
        final IResourceStream resourceStream = new StringResourceStream(clob.getChars(), clob.getMimeType().toString());
        return resourceStream;
    }

    private static IRequestHandler handlerFor(final IResourceStream resourceStream, final NamedWithMimeType namedWithMimeType) {
        final ResourceStreamRequestHandler handler =
                new ResourceStreamRequestHandler(resourceStream, namedWithMimeType.getName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        return handler;
    }

    //////////////////////////////////////////////////

    @Override
    public PromptStyle getPromptStyle() {
        final ObjectAction objectAction = getMetaModel();
        final ObjectSpecification objectActionOwner = objectAction.getOnType();
        if(objectActionOwner.isManagedBean()) {
            // tried to move this test into PromptStyleFacetFallback,
            // however it's not that easy to lookup the owning type
            final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
            if (facet != null) {
                final PromptStyle promptStyle = facet.value();
                if (promptStyle.isDialog()) {
                    // could be specified explicitly.
                    return promptStyle;
                }
            }
            return PromptStyle.DIALOG;
        }
        if(objectAction.getParameterCount() == 0) {
            // a bit of a hack, the point being that the UI for dialog correctly handles no-args,
            // whereas for INLINE it would render a form with no fields
            return PromptStyle.DIALOG;
        }
        final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
        if(facet == null) {
            // don't think this can happen actually, see PromptStyleFacetFallback
            return PromptStyle.INLINE;
        }
        final PromptStyle promptStyle = facet.value();
        if (promptStyle == PromptStyle.AS_CONFIGURED) {
            // I don't think this can happen, actually...
            // when the metamodel is built, it should replace AS_CONFIGURED with one of the other prompts
            // (see PromptStyleConfiguration and PromptStyleFacetFallback)
            return PromptStyle.INLINE;
        }
        return promptStyle;
    }

    public <T extends Facet> T getFacet(final Class<T> facetType) {
        final FacetHolder facetHolder = getMetaModel();
        return facetHolder.getFacet(facetType);
    }


    //////////////////////////////////////////////////

    private InlinePromptContext inlinePromptContext;

    /**
     * Further hint, to support inline prompts...
     */
    @Override
    public InlinePromptContext getInlinePromptContext() {
        return inlinePromptContext;
    }

    public void setInlinePromptContext(InlinePromptContext inlinePromptContext) {
        this.inlinePromptContext = inlinePromptContext;
    }

    public void setParameterValue(ObjectActionParameter actionParameter, ManagedObject newParamValue) {
        argCache().setParameterValue(actionParameter, newParamValue);
    }

    public void clearParameterValue(ObjectActionParameter actionParameter) {
        argCache().clearParameterValue(actionParameter);
    }

    @Override
    public Stream<FormPendingParamUiModel> streamPendingParamUiModels() {

        val owner = this.getOwner();
        val target = this.getMetaModel().realTargetAdapter(owner);
        val pendingArgs = getArgumentsAsParamModel();

        val head = InteractionHead.of(owner, target);
        
        return argCache()
        .streamParamUiModels()
        .map(paramUiModel->{
            return FormPendingParamUiModel.of(head, paramUiModel, pendingArgs);
        });

    }

    public void reassessPendingParamUiModels(int skipCount) {

        val pendingArgs = getArgumentsAsParamModel();

        argCache()
        .streamParamUiModels()
        .skip(skipCount)
        .forEach(actionArgumentModel->{

            val actionParameter = actionArgumentModel.getMetaModel();
            val paramValue = actionArgumentModel.getValue();
            val hasChoices = actionParameter.hasChoices();
            val hasAutoComplete = actionParameter.hasAutoComplete();
            val isEmpty = ManagedObjects.isNullOrUnspecifiedOrEmpty(paramValue);
            // if we have choices or autoSelect, don't override any param value, already chosen by the user
            val vetoDefaultsToBeSet = !isEmpty 
                    && (hasChoices||hasAutoComplete);
            
            if(!vetoDefaultsToBeSet) {
                val paramDefaultValue = actionParameter.getDefault(pendingArgs);
                if (ManagedObjects.isNullOrUnspecifiedOrEmpty(paramDefaultValue)) {
                    clearParameterValue(actionParameter);
                } else {
                    setParameterValue(actionParameter, paramDefaultValue);
                }
                return;
            }
            
            boolean shouldBlankout = false;

            if(!isEmpty) {
                if(hasChoices) {
                    // make sure the object is one of the choices, else blank it out.
                    
                    val choices = actionParameter
                            .getChoices(pendingArgs, InteractionInitiatedBy.USER);

                    shouldBlankout = 
                            ! isPartOfChoicesConsideringDependentArgs(paramValue, choices);

                } else if(hasAutoComplete) {

                    //XXX poor man's implementation: don't blank-out, even though could fail validation later 
                    shouldBlankout = false;
                }
            }

            if(shouldBlankout) {
                clearParameterValue(actionParameter);
            }

        });

    }

    private boolean isPartOfChoicesConsideringDependentArgs(
            ManagedObject paramValue, 
            Can<ManagedObject> choices) {

        val pendingValue = paramValue.getPojo();

        return choices
                .stream()
                .anyMatch(choice->Objects.equals(pendingValue, choice.getPojo()));
    }


}
