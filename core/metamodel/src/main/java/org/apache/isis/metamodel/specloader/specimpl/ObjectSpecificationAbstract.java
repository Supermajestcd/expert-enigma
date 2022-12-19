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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.exceptions.UnknownTypeException;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.collections._Streams;
import org.apache.isis.commons.internal.ioc.BeanAdapter;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.metamodel.JdoMetamodelUtil;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.commons.ClassExtensions;
import org.apache.isis.metamodel.commons.ToString;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.interactions.InteractionContext;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.metamodel.layout.DeweyOrderSet;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.security.authentication.AuthenticationSession;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2 @EqualsAndHashCode(of = "correspondingClass", callSuper = false)
public abstract class ObjectSpecificationAbstract extends FacetHolderImpl implements ObjectSpecification {

    private static class Subclasses {
        private final Set<ObjectSpecification> classes = _Sets.newConcurrentHashSet();

        public void addSubclass(final ObjectSpecification subclass) {
            if(classes.contains(subclass)) {
                return;
            }
            classes.add(subclass);
        }

        public boolean hasSubclasses() {
            return !classes.isEmpty();
        }

        public Collection<ObjectSpecification> toCollection() {
            return Collections.unmodifiableSet(classes);
        }
    }

//    private static class Subclasses {
//        private final List<ObjectSpecification> classes = new ArrayList<>();
//
//        public void addSubclass(final ObjectSpecification subclass) {
//            if(classes.contains(subclass)) {
//                return;
//            }
//            classes.add(subclass);
//        }
//
//        public boolean hasSubclasses() {
//            return !classes.isEmpty();
//        }
//
//        public List<ObjectSpecification> toCollection() {
//            return Collections.unmodifiableList(classes);
//        }
//    }
    
    
    // -- fields

    //protected final ServiceInjector servicesInjector;

    private final MetaModelContext context;
    private PostProcessor postProcessor;
    private final SpecificationLoader specificationLoader;
    private final FacetProcessor facetProcessor;

    // -- ASSOCIATIONS
    
    private final List<ObjectAssociation> associations = _Lists.newArrayList();
    
    // defensive immutable lazy copy of associations
    private final _Lazy<List<ObjectAssociation>> unmodifiableAssociations = 
            _Lazy.threadSafe(()->Collections.unmodifiableList(new ArrayList<>(associations)));
    
    // -- ACTIONS
    
    private final List<ObjectAction> objectActions = _Lists.newArrayList();

    // defensive immutable lazy copy of objectActions
    private final _Lazy<List<ObjectAction>> unmodifiableActions = 
            _Lazy.threadSafe(()->Collections.unmodifiableList(new ArrayList<>(objectActions)));

    // partitions and caches objectActions by type; updated in sortCacheAndUpdateActions()
    private final ListMultimap<ActionType, ObjectAction> objectActionsByType = 
            _Multimaps.newConcurrentListMultimap();
    
    // -- INTERFACES

    private final List<ObjectSpecification> interfaces = _Lists.newArrayList();
    
    // defensive immutable lazy copy of interfaces
    private final _Lazy<List<ObjectSpecification>> unmodifiableInterfaces = 
            _Lazy.threadSafe(()->Collections.unmodifiableList(new ArrayList<>(interfaces)));
    
    
    
    private final Subclasses directSubclasses = new Subclasses();
    // built lazily
    private Subclasses transitiveSubclasses;

    private final Class<?> correspondingClass;
    private final String fullName;
    private final String shortName;
    private final Identifier identifier;
    private final boolean isAbstract;

    @Getter(onMethod=@__({@Override})) private final boolean excludedFromMetamodel;
    // derived lazily, cached since immutable
    protected ObjectSpecId specId;

    private ObjectSpecification superclassSpec;

    private TitleFacet titleFacet;
    private IconFacet iconFacet;
    private NavigableParentFacet navigableParentFacet;
    private CssClassFacet cssClassFacet;

    private IntrospectionState introspectionState = IntrospectionState.NOT_INTROSPECTED;

    private final static Set<String> exclusions = _Sets.of( //TODO[2133] make this configurable, or find an alternative, perhaps a specific type annotation?
            "org.apache.isis.extensions.fixtures.fixturescripts.FixtureResult",
            "org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript"
            );
    

    // -- Constructor
    public ObjectSpecificationAbstract(
            final Class<?> introspectedClass,
            final String shortName,
            final FacetProcessor facetProcessor,
            final PostProcessor postProcessor) {

        this.correspondingClass = introspectedClass;
        this.fullName = introspectedClass.getName();
        this.shortName = shortName;

        this.isAbstract = ClassExtensions.isAbstract(introspectedClass);
        this.excludedFromMetamodel = _Reflect
                .streamTypeHierarchy(introspectedClass, /*includeInterfaces*/ false)
                .anyMatch(type->exclusions.contains(type.getName())); 

        this.identifier = Identifier.classIdentifier(introspectedClass);

        this.facetProcessor = facetProcessor;

        this.context = MetaModelContext.current();
        this.specificationLoader = context.getSpecificationLoader();
        this.postProcessor = postProcessor;
    }

    // -- Stuff immediately derivable from class
    @Override
    public FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    public ObjectSpecId getSpecId() {
        if(specId == null) {
            final ObjectSpecIdFacet facet = getFacet(ObjectSpecIdFacet.class);
            if(facet == null) {
                throw new IllegalStateException("could not find an ObjectSpecIdFacet for " + this.getFullIdentifier());
            }
            specId = facet.value();
        }
        return specId;
    }

    /**
     * As provided explicitly within the constructor.
     *
     * <p>
     * Not API, but <tt>public</tt> so that {@link FacetedMethodsBuilder} can
     * call it.
     */
    @Override
    public Class<?> getCorrespondingClass() {
        return correspondingClass;
    }

    @Override
    public String getShortIdentifier() {
        return shortName;
    }

    /**
     * The {@link Class#getName() (full) name} of the
     * {@link #getCorrespondingClass() class}.
     */
    @Override
    public String getFullIdentifier() {
        return fullName;
    }

    @Override
    public void introspectUpTo(final IntrospectionState upTo) {
        
        if(!isLessThan(upTo)) {
            return; // optimization
        }

        if(log.isDebugEnabled()) {
            log.debug("introspectingUpTo: {}, {}", getFullIdentifier(), upTo);
        }
        
        switch (introspectionState) {
        case NOT_INTROSPECTED:
            if(isLessThan(upTo)) {
                // set to avoid infinite loops
                this.introspectionState = IntrospectionState.TYPE_BEING_INTROSPECTED;
                introspectTypeHierarchy();
                updateFromFacetValues();
                this.introspectionState = IntrospectionState.TYPE_INTROSPECTED;
            }
            if(isLessThan(upTo)) {
                this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
                introspectMembers();
                this.introspectionState = IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED;
            }
            // set to avoid infinite loops
            break;
        case TYPE_BEING_INTROSPECTED:
            // nothing to do
            break;
        case TYPE_INTROSPECTED:
            if(isLessThan(upTo)) {
                // set to avoid infinite loops
                this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
                introspectMembers();
                this.introspectionState = IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED;
            }
            break;
        case MEMBERS_BEING_INTROSPECTED:
            // nothing to do
        case TYPE_AND_MEMBERS_INTROSPECTED:
            // nothing to do
            break;
        }
    }

    private boolean isLessThan(IntrospectionState upTo) {
        return this.introspectionState.compareTo(upTo) < 0;
    }
    
    
    protected abstract void introspectTypeHierarchy();
    protected abstract void introspectMembers();


    protected void loadSpecOfSuperclass(final Class<?> superclass) {
        if (superclass == null) {
            return;
        }
        superclassSpec = getSpecificationLoader().loadSpecification(superclass);
        if (superclassSpec != null) {
            if (log.isDebugEnabled()) {
                log.debug("  Superclass {}", superclass.getName());
            }
            updateAsSubclassTo(superclassSpec);
        }
    }

    protected void updateInterfaces(final List<ObjectSpecification> interfaces) {
        synchronized(unmodifiableInterfaces) {
            this.interfaces.clear();
            this.interfaces.addAll(interfaces);
            unmodifiableInterfaces.clear();
        }
    }

    private void updateAsSubclassTo(final ObjectSpecification supertypeSpec) {
        if (!(supertypeSpec instanceof ObjectSpecificationAbstract)) {
            return;
        }
        // downcast required because addSubclass is (deliberately) not public
        // API
        final ObjectSpecificationAbstract introspectableSpec = (ObjectSpecificationAbstract) supertypeSpec;
        introspectableSpec.updateSubclasses(this);
    }

    protected void updateAsSubclassTo(final List<ObjectSpecification> supertypeSpecs) {
        for (final ObjectSpecification supertypeSpec : supertypeSpecs) {
            updateAsSubclassTo(supertypeSpec);
        }
    }

    private void updateSubclasses(final ObjectSpecification subclass) {
        this.directSubclasses.addSubclass(subclass);
    }

    protected void sortAndUpdateAssociations(final List<ObjectAssociation> associations) {
        val orderedAssociations = sortAssociations(associations);
        synchronized (unmodifiableAssociations) {
            this.associations.clear();
            this.associations.addAll(orderedAssociations);
            unmodifiableAssociations.clear(); // invalidate
        }
    }

    protected void sortCacheAndUpdateActions(final List<ObjectAction> objectActions) {
        final List<ObjectAction> orderedActions = sortActions(objectActions);
        synchronized (unmodifiableActions){
            this.objectActions.clear();
            this.objectActions.addAll(orderedActions);
            unmodifiableActions.clear();

            for (val actionType : ActionType.values()) {
                val objectActionForType = objectActionsByType.getOrElseNew(actionType);
                objectActionForType.clear();
                objectActions.stream()
                .filter(ObjectAction.Predicates.ofType(actionType))
                .forEach(objectActionForType::add);
            }
        }
    }


    private void updateFromFacetValues() {

        titleFacet = getFacet(TitleFacet.class);
        iconFacet = getFacet(IconFacet.class);
        navigableParentFacet = getFacet(NavigableParentFacet.class);
        cssClassFacet = getFacet(CssClassFacet.class);
    }


    protected void postProcess() {
        postProcessor.postProcess(this);
        updateFromFacetValues();
    }


    @Override
    public String getTitle(
            ManagedObject contextAdapterIfAny,
            ManagedObject targetAdapter) {
        if (titleFacet != null) {
            final String titleString = titleFacet.title(contextAdapterIfAny, targetAdapter);
            if (!_Strings.isEmpty(titleString)) {
                return titleString;
            }
        }
        return (this.isManagedBean() ? "" : "Untitled ") + getSingularName();
    }


    @Override
    public String getIconName(final ManagedObject reference) {
        return iconFacet == null ? null : iconFacet.iconName(reference);
    }

    @Override
    public Object getNavigableParent(final Object object) {
        return navigableParentFacet == null
                ? null
                        : navigableParentFacet.navigableParent(object);
    }

    @Override
    public String getCssClass(final ManagedObject reference) {
        return cssClassFacet == null ? null : cssClassFacet.cssClass(reference);
    }


    // -- Hierarchical
    /**
     * Determines if this class represents the same class, or a subclass, of the
     * specified class.
     *
     * <p>
     * cf {@link Class#isAssignableFrom(Class)}, though target and parameter are
     * the opposite way around, ie:
     *
     * <pre>
     * cls1.isAssignableFrom(cls2);
     * </pre>
     * <p>
     * is equivalent to:
     *
     * <pre>
     * spec2.isOfType(spec1);
     * </pre>
     *
     */
    @Override
    public boolean isOfType(final ObjectSpecification specification) {
        // do the comparison using value types because of a possible aliasing/race condition
        // in matchesParameterOf when building up contributed associations
        if (specification.getSpecId().equals(this.getSpecId())) {
            return true;
        }
        for (final ObjectSpecification interfaceSpec : interfaces()) {
            if (interfaceSpec.isOfType(specification)) {
                return true;
            }
        }

        // this is a bit of a workaround; the metamodel doesn't have the interfaces for enums.
        final Class<?> correspondingClass = getCorrespondingClass();
        final Class<?> possibleSupertypeClass = specification.getCorrespondingClass();
        if(correspondingClass != null && possibleSupertypeClass != null &&
                Enum.class.isAssignableFrom(correspondingClass) && possibleSupertypeClass.isInterface()) {
            if(possibleSupertypeClass.isAssignableFrom(correspondingClass)) {
                return true;
            }
        }

        final ObjectSpecification superclassSpec = superclass();
        return superclassSpec != null && superclassSpec.isOfType(specification);
    }



    // -- Name, Description, Persistability
    /**
     * The name according to any available {@link org.apache.isis.metamodel.facets.all.named.NamedFacet},
     * but falling back to {@link #getFullIdentifier()} otherwise.
     */
    @Override
    public String getSingularName() {
        final NamedFacet namedFacet = getFacet(NamedFacet.class);
        return namedFacet != null? namedFacet.value() : this.getFullIdentifier();
    }

    /**
     * The pluralized name according to any available {@link org.apache.isis.metamodel.facets.object.plural.PluralFacet},
     * else <tt>null</tt>.
     */
    @Override
    public String getPluralName() {
        final PluralFacet pluralFacet = getFacet(PluralFacet.class);
        return pluralFacet.value();
    }

    /**
     * The description according to any available {@link org.apache.isis.metamodel.facets.object.plural.PluralFacet},
     * else empty string (<tt>""</tt>).
     */
    @Override
    public String getDescription() {
        final DescribedAsFacet describedAsFacet = getFacet(DescribedAsFacet.class);
        final String describedAs = describedAsFacet.value();
        return describedAs == null ? "" : describedAs;
    }

    /*
     * help is typically a reference (eg a URL) and so should not default to a
     * textual value if not set up
     */
    @Override
    public String getHelp() {
        final HelpFacet helpFacet = getFacet(HelpFacet.class);
        return helpFacet == null ? null : helpFacet.value();
    }




    // -- Facet Handling

    @Override
    public <Q extends Facet> Q getFacet(final Class<Q> facetType) {

        synchronized(unmodifiableInterfaces) {
        
            // lookup facet holder's facet
            val facets1 = _NullSafe.streamNullable(super.getFacet(facetType));
    
            // lookup all interfaces
            val facets2 = _NullSafe.stream(interfaces())
                    .filter(_NullSafe::isPresent) // just in case
                    .map(interfaceSpec->interfaceSpec.getFacet(facetType));
    
            // search up the inheritance hierarchy
            val facets3 = _NullSafe.streamNullable(superclass()) 
                    .map(superSpec->superSpec.getFacet(facetType));
    
            val facetsCombined = _Streams.concat(facets1, facets2, facets3);
    
            val notANoopFacetFilter = new NotANoopFacetFilter<Q>();
    
            return facetsCombined
                    .filter(notANoopFacetFilter)
                    .findFirst()
                    .orElse(notANoopFacetFilter.noopFacet);
        
        }
    }

    @Vetoed
    private static class NotANoopFacetFilter<Q extends Facet> implements Predicate<Q> {
        Q noopFacet;

        @Override
        public boolean test(Q facet) {
            if(facet==null) {
                return false;
            }
            if(!facet.isNoop()) {
                return true;
            }
            if(noopFacet == null) {
                noopFacet = facet;
            }
            return false;
        }
    }


    // -- DefaultValue - unused
    /**
     * @deprecated  - never called.
     * @return - always returns <tt>null</tt>
     */
    @Deprecated
    @Override
    public Object getDefaultValue() {
        return null;
    }


    // -- Identifier
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }



    // -- createTitleInteractionContext
    @Override
    public ObjectTitleContext createTitleInteractionContext(
            final AuthenticationSession session, 
            final InteractionInitiatedBy interactionMethod, 
            final ManagedObject targetObjectAdapter) {

        return new ObjectTitleContext(targetObjectAdapter, getIdentifier(), targetObjectAdapter.titleString(null),
                interactionMethod);
    }



    // -- Superclass, Interfaces, Subclasses, isAbstract
    @Override
    public ObjectSpecification superclass() {
        return superclassSpec;
    }

    @Override
    public Collection<ObjectSpecification> interfaces() {
        return unmodifiableInterfaces.get();
    }

    @Override
    public Collection<ObjectSpecification> subclasses(final Depth depth) {
        if (depth == Depth.DIRECT) {
            return directSubclasses.toCollection();
        }

        // depth == Depth.TRANSITIVE)
        if (transitiveSubclasses == null) {
            transitiveSubclasses = transitiveSubclasses();
        }

        return transitiveSubclasses.toCollection();
    }

    private synchronized Subclasses transitiveSubclasses() {
        final Subclasses appendTo = new Subclasses();
        appendSubclasses(this, appendTo);
        transitiveSubclasses = appendTo;
        return transitiveSubclasses;
    }

    private void appendSubclasses(
            final ObjectSpecification objectSpecification,
            final Subclasses appendTo) {

        val directSubclasses = objectSpecification.subclasses(Depth.DIRECT);
        for (ObjectSpecification subclass : directSubclasses) {
            appendTo.addSubclass(subclass);
            appendSubclasses(subclass, appendTo);
        }

    }

    @Override
    public boolean hasSubclasses() {
        return directSubclasses.hasSubclasses();
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    // -- Associations

    @Override
    public Stream<ObjectAssociation> streamAssociations(final Contributed contributed) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        guardAgainstTooEarly_assoz(contributed);
        
        synchronized(unmodifiableAssociations) {
            return stream(unmodifiableAssociations.get())
                    .filter(ContributeeMember.Predicates.regularElse(contributed));    
        }
        
    }

    @Override
    public ObjectMember getMember(final String memberId) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        final ObjectAction objectAction = getObjectAction(memberId);
        if(objectAction != null) {
            return objectAction;
        }
        final ObjectAssociation association = getAssociation(memberId);
        if(association != null) {
            return association;
        }
        return null;
    }


    /**
     * The association with the given {@link ObjectAssociation#getId() id}.
     *
     * <p>
     * This is overridable because {@link org.apache.isis.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList}
     * simply returns <tt>null</tt>.
     *
     * <p>
     * TODO put fields into hash.
     *
     * <p>
     * TODO: could this be made final? (ie does the framework ever call this
     * method for an {@link org.apache.isis.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList})
     */
    @Override
    public ObjectAssociation getAssociation(final String id) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        ObjectAssociation oa = getAssociationWithId(id);
        if(oa != null) {
            return oa;
        }
        //TODO [2033] remove or replace        
        //        if(IsisSystemEnvironment.get().isPrototyping()) {
        //            // automatically refresh if not in production
        //            // (better support for jrebel)
        //
        //            LOG.warn("Could not find association with id '{}'; invalidating cache automatically", id);
        //            if(!invalidatingCache.get()) {
        //                // make sure don't go into an infinite loop, though.
        //                try {
        //                    invalidatingCache.set(true);
        //                    getSpecificationLoader().invalidateCache(getCorrespondingClass());
        //                } finally {
        //                    invalidatingCache.set(false);
        //                }
        //            } else {
        //                LOG.warn("... already invalidating cache earlier in stacktrace, so skipped this time");
        //            }
        //            oa = getAssociationWithId(id);
        //            if(oa != null) {
        //                return oa;
        //            }
        //        }
        throw new ObjectSpecificationException(
                String.format("No association called '%s' in '%s'", id, getSingularName()));
    }

    private ObjectAssociation getAssociationWithId(final String id) {
        return streamAssociations(Contributed.INCLUDED)
                .filter(objectAssociation->objectAssociation.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Stream<ObjectAction> streamObjectActions(final ActionType type, final Contributed contributed) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        guardAgainstTooEarly_contrib(contributed);

        return stream(objectActionsByType.get(type))
                .filter(ContributeeMember.Predicates.regularElse(contributed));
    }

    // -- sorting

    private List<ObjectAssociation> sortAssociations(final List<ObjectAssociation> associations) {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(associations);
        final List<ObjectAssociation> orderedAssociations = _Lists.newArrayList();
        sortAssociations(orderSet, orderedAssociations);
        return orderedAssociations;
    }

    private static void sortAssociations(final DeweyOrderSet orderSet, final List<ObjectAssociation> associationsToAppendTo) {
        for (final Object element : orderSet) {
            if (element instanceof OneToManyAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof OneToOneAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof DeweyOrderSet) {
                // just flatten.
                DeweyOrderSet childOrderSet = (DeweyOrderSet) element;
                sortAssociations(childOrderSet, associationsToAppendTo);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

    private static List<ObjectAction> sortActions(final List<ObjectAction> actions) {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(actions);
        final List<ObjectAction> orderedActions = _Lists.newArrayList();
        sortActions(orderSet, orderedActions);
        return orderedActions;
    }

    private static void sortActions(final DeweyOrderSet orderSet, final List<ObjectAction> actionsToAppendTo) {
        for (final Object element : orderSet) {
            if(element instanceof ObjectAction) {
                final ObjectAction objectAction = (ObjectAction) element;
                actionsToAppendTo.add(objectAction);
            }
            else if (element instanceof DeweyOrderSet) {
                final DeweyOrderSet set = ((DeweyOrderSet) element);
                final List<ObjectAction> actions = _Lists.newArrayList();
                sortActions(set, actions);
                actionsToAppendTo.addAll(actions);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

    private Stream<BeanAdapter> streamServiceBeans() {
        return context.getServiceRegistry().streamRegisteredBeansOfSort(BeanSort.MANAGED_BEAN);
    }

    // -- contributee associations (properties and collections)

    private List<ObjectAssociation> createContributeeAssociations() {
        if (isManagedBean() || isValue()) {
            return Collections.emptyList();
        }
        final List<ObjectAssociation> contributeeAssociations = _Lists.newArrayList();
        streamServiceBeans()
        .forEach(serviceBean->addContributeeAssociationsIfAny(serviceBean, contributeeAssociations));
        return contributeeAssociations;
    }

    private void addContributeeAssociationsIfAny(
            final BeanAdapter serviceBean, 
            final List<ObjectAssociation> contributeeAssociationsToAppendTo) {

        final Class<?> serviceClass = serviceBean.getBeanClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(serviceClass,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (specification == this) {
            return;
        }
        final List<ObjectAssociation> contributeeAssociations = createContributeeAssociations(serviceBean);
        contributeeAssociationsToAppendTo.addAll(contributeeAssociations);
    }

    private boolean canAdd(ObjectAction serviceAction) {
        if (isAlwaysHidden(serviceAction)) {
            return false;
        }
        final NotContributedFacet notContributed = serviceAction.getFacet(NotContributedFacet.class);
        if(notContributed != null && notContributed.toAssociations()) {
            return false;
        }
        if(!serviceAction.hasReturn()) {
            return false;
        }
        if (serviceAction.getParameterCount() != 1 || contributeeParameterMatchOf(serviceAction) == -1) {
            return false;
        }
        if(!(serviceAction instanceof ObjectActionDefault)) {
            return false;
        }
        if(!serviceAction.getSemantics().isSafeInNature()) {
            return false;
        }
        return true;
    }

    private List<ObjectAssociation> createContributeeAssociations(final BeanAdapter serviceBean) {
        final Class<?> serviceClass = serviceBean.getBeanClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(serviceClass,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        final Stream<ObjectAction> serviceActions = specification
                .streamObjectActions(ActionType.USER, Contributed.INCLUDED);

        return serviceActions
                .filter(this::canAdd)
                .map(serviceAction->(ObjectActionDefault) serviceAction)
                .map(createContributeeAssociationFunctor(serviceBean, this))
                .collect(Collectors.toList());

    }

    private Function<ObjectActionDefault, ObjectAssociation> createContributeeAssociationFunctor(
            final BeanAdapter serviceBean,
            final ObjectSpecification contributeeType) {

        return new Function<ObjectActionDefault, ObjectAssociation>(){
            @Override
            public ObjectAssociation apply(ObjectActionDefault input) {
                final ObjectSpecification returnType = input.getReturnType();
                final ObjectAssociationAbstract association = createObjectAssociation(input, returnType);
                facetProcessor.processMemberOrder(association);
                return association;
            }

            private ObjectAssociationAbstract createObjectAssociation(
                    final ObjectActionDefault input,
                    final ObjectSpecification returnType) {
                if (returnType.isNotCollection()) {
                    return new OneToOneAssociationContributee(serviceBean, input, contributeeType);
                } else {
                    return new OneToManyAssociationContributee(serviceBean, input, contributeeType);
                }
            }
        };
    }



    // -- mixin associations (properties and collections)

    private List<ObjectAssociation> createMixedInAssociations() {
        if (isManagedBean() || isValue()) {
            return Collections.emptyList();
        }

        val mixinTypes = IsisBeanTypeRegistry.current().getMixinTypes();
        if(_NullSafe.isEmpty(mixinTypes)) {
            return Collections.emptyList();
        }

        val mixedInAssociations = _Lists.<ObjectAssociation>newArrayList();

        for (final Class<?> mixinType : mixinTypes) {
            addMixedInAssociationsIfAny(mixinType, mixedInAssociations);
        }
        return mixedInAssociations;
    }

    private void addMixedInAssociationsIfAny(
            final Class<?> mixinType, 
            final List<ObjectAssociation> toAppendTo) {

        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(mixinType,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (specification == this) {
            return;
        }
        final MixinFacet mixinFacet = specification.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            // this shouldn't happen; perhaps it would be more correct to throw an exception?
            return;
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return;
        }

        final Stream<ObjectActionDefault> mixinActions = objectActionsOf(specification);

        mixinActions
        .filter((ObjectActionDefault input) -> {
            final NotContributedFacet notContributedFacet = input.getFacet(NotContributedFacet.class);
            if (notContributedFacet == null || !notContributedFacet.toActions()) {
                return false;
            }
            if(input.getParameterCount() != 0) {
                return false;
            }
            if(!input.getSemantics().isSafeInNature()) {
                return false;
            }
            return true;
        })
        .map(createMixedInAssociationFunctor(this, mixinType, mixinFacet.value()))
        .forEach(toAppendTo::add);

    }

    private Stream<ObjectActionDefault> objectActionsOf(final ObjectSpecification specification) {
        return specification.streamObjectActions(ActionType.ALL, Contributed.INCLUDED)
                .map(a->(ObjectActionDefault)a);
    }

    private Function<ObjectActionDefault, ObjectAssociation> createMixedInAssociationFunctor(
            final ObjectSpecification mixedInType,
            final Class<?> mixinType,
            final String mixinMethodName) {
        return new Function<ObjectActionDefault, ObjectAssociation>(){
            @Override
            public ObjectAssociation apply(final ObjectActionDefault mixinAction) {
                final ObjectAssociationAbstract association = createObjectAssociation(mixinAction);
                facetProcessor.processMemberOrder(association);
                return association;
            }

            ObjectAssociationAbstract createObjectAssociation(
                    final ObjectActionDefault mixinAction) {
                final ObjectSpecification returnType = mixinAction.getReturnType();
                if (returnType.isNotCollection()) {
                    return new OneToOneAssociationMixedIn(
                            mixinAction, mixedInType, mixinType, mixinMethodName);
                } else {
                    return new OneToManyAssociationMixedIn(
                            mixinAction, mixedInType, mixinType, mixinMethodName);
                }
            }
        };
    }



    // -- contributee actions
    /**
     * All contributee actions (each wrapping a service's contributed action) for this spec.
     *
     * <p>
     * If this specification {@link #isManagedBean() is actually for} a service,
     * then returns an empty list.
     */
    private List<ObjectAction> createContributeeActions() {
        if (isManagedBean() || isValue()) {
            return Collections.emptyList();
        }
        final List<ObjectAction> contributeeActions = _Lists.newArrayList();
        streamServiceBeans()
        .forEach(serviceBean->addContributeeActionsIfAny(serviceBean, contributeeActions));
        return contributeeActions;
    }

    private boolean canAddContributee(final ObjectAction serviceAction) {
        if (isAlwaysHidden(serviceAction)) {
            return false;
        }
        final NotContributedFacet notContributed = serviceAction.getFacet(NotContributedFacet.class);
        if(notContributed != null && notContributed.toActions()) {
            return false;
        }
        if(!(serviceAction instanceof ObjectActionDefault)) {
            return false;
        }
        return true;
    }

    private void addContributeeActionsIfAny(
            final Object servicePojo,
            final List<ObjectAction> contributeeActionsToAppendTo) {

        if(log.isDebugEnabled()) {
            log.debug("{} : addContributeeActionsIfAny(...); servicePojo class is: {}", 
                    this.getFullIdentifier(), servicePojo.getClass().getName());
        }

        final Class<?> serviceType = servicePojo.getClass();
        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(serviceType,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (specification == this) {
            return;
        }

        final Stream<ObjectAction> serviceActions = specification
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);

        serviceActions
        .filter(this::canAddContributee)
        .map(serviceAction->(ObjectActionDefault) serviceAction)
        .forEach(contributedAction->{

            // see if qualifies by inspecting all parameters
            final int contributeeParam = contributeeParameterMatchOf(contributedAction);
            if(contributeeParam == -1) {
                return;
            }

            ObjectActionContributee contributeeAction =
                    new ObjectActionContributee(servicePojo, contributedAction, contributeeParam, this);
            facetProcessor.processMemberOrder(contributeeAction);
            contributeeActionsToAppendTo.add(contributeeAction);
        });

    }

    private boolean isAlwaysHidden(final FacetHolder holder) {
        final HiddenFacet hiddenFacet = holder.getFacet(HiddenFacet.class);
        return hiddenFacet != null && hiddenFacet.where() == Where.ANYWHERE;
    }



    /**
     * @param serviceAction - number of the parameter that matches, or -1 if none.
     */
    private int contributeeParameterMatchOf(final ObjectAction serviceAction) {
        final List<ObjectActionParameter> params = serviceAction.getParameters();
        for (final ObjectActionParameter param : params) {
            if (isOfType(param.getSpecification())) {
                return param.getNumber();
            }
        }
        return -1;
    }


    // -- mixin actions
    /**
     * All contributee actions (each wrapping a service's contributed action) for this spec.
     *
     * <p>
     * If this specification {@link #isManagedBean() is actually for} a service,
     * then returns an empty list.
     */
    private List<ObjectAction> createMixedInActions() {
        if (isManagedBean() || isValue() || isMixin()) {
            return Collections.emptyList();
        }

        val mixinTypes = IsisBeanTypeRegistry.current().getMixinTypes();
        if(_NullSafe.isEmpty(mixinTypes)) {
            return Collections.emptyList();
        }

        val mixedInActions = _Lists.<ObjectAction>newArrayList();
        for (final Class<?> mixinType : mixinTypes) {
            addMixedInActionsIfAny(mixinType, mixedInActions);
        }
        return mixedInActions;
    }

    private boolean canAddMixin(ObjectAction mixinTypeAction) {
        if (isAlwaysHidden(mixinTypeAction)) {
            return false;
        }
        if(!(mixinTypeAction instanceof ObjectActionDefault)) {
            return false;
        }
        final ObjectActionDefault mixinAction = (ObjectActionDefault) mixinTypeAction;
        final NotContributedFacet notContributedFacet = mixinAction.getFacet(NotContributedFacet.class);
        if(notContributedFacet != null && notContributedFacet.toActions()) {
            return false;
        }
        return true;
    }

    private void addMixedInActionsIfAny(
            final Class<?> mixinType,
            final List<ObjectAction> mixedInActionsToAppendTo) {

        final ObjectSpecification mixinSpec = getSpecificationLoader().loadSpecification(mixinType,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (mixinSpec == this) {
            return;
        }
        final MixinFacet mixinFacet = mixinSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            // this shouldn't happen; perhaps it would be more correct to throw an exception?
            return;
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return;
        }

        final Stream<ObjectAction> mixinActions = mixinSpec
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);

        mixinActions
        .filter(this::canAddMixin)
        .forEach(mixinTypeAction->{
            ObjectActionMixedIn mixedInAction =
                    new ObjectActionMixedIn(mixinType, mixinFacet.value(), (ObjectActionDefault)mixinTypeAction, this);
            facetProcessor.processMemberOrder(mixedInAction);
            mixedInActionsToAppendTo.add(mixedInAction);
        });

    }



    // -- validity
    @Override
    public Consent isValid(final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return isValidResult(targetAdapter, interactionInitiatedBy).createConsent();
    }

    @Override
    public InteractionResult isValidResult(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ObjectValidityContext validityContext =
                createValidityInteractionContext(
                        targetAdapter, interactionInitiatedBy);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    /**
     * Create an {@link InteractionContext} representing an attempt to save the
     * object.
     */
    @Override
    public ObjectValidityContext createValidityInteractionContext(
            final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return new ObjectValidityContext(targetAdapter, getIdentifier(), interactionInitiatedBy);
    }

    protected BeanSort managedObjectSort; 

    @Override
    public BeanSort getBeanSort() {
        if(managedObjectSort==null) {
            managedObjectSort = sortOf(this);
        }
        return managedObjectSort;
    }

    // -- convenience isXxx (looked up from facets)
    @Override
    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    @Override
    public boolean isHidden() {
        return containsFacet(HiddenFacet.class);
    }

    @Override
    public boolean isParseable() {
        return containsFacet(ParseableFacet.class);
    }

    @Override
    public boolean isEncodeable() {
        return containsFacet(EncodableFacet.class);
    }

    @Override
    public boolean isParented() {
        return containsFacet(ParentedCollectionFacet.class);
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        return str.toString();
    }
    
    // -- GUARDS

    private boolean contributeeAndMixedInAssociationsAdded;
    private boolean contributeeAndMixedInActionsAdded;

    private void guardAgainstTooEarly_contrib(Contributed contributed) {
        // update our list of actions if requesting for contributed actions
        // and they have not yet been added
        // the "contributed.isIncluded()" guard is required because we cannot do this too early;
        // there must be a session available
        if(contributed.isIncluded() && !contributeeAndMixedInActionsAdded) {
            synchronized (unmodifiableActions) {
                val actions = _Lists.newArrayList(this.objectActions);
                if (isEntityOrViewModel()) {
                    actions.addAll(createContributeeActions());
                    actions.addAll(createMixedInActions());
                }
                sortCacheAndUpdateActions(actions);
                contributeeAndMixedInActionsAdded = true;
            }
        }
    }

    private void guardAgainstTooEarly_assoz(Contributed contributed) {
        // the "contributed.isIncluded()" guard is required because we cannot do this too early;
        // there must be a session available
        if(contributed.isIncluded() && !contributeeAndMixedInAssociationsAdded) {
            synchronized (unmodifiableAssociations) {
                val associations = _Lists.newArrayList(this.associations);
                if(isEntityOrViewModel()) {
                    associations.addAll(createContributeeAssociations());
                    associations.addAll(createMixedInAssociations());
                }
                sortAndUpdateAssociations(associations);
                contributeeAndMixedInAssociationsAdded = true;
            }
        }
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    protected BeanSort sortOf(ObjectSpecification spec) {
        //TODO [2033] this is the way we want it to work in the future; 
        //	by now we do prime the #managedObjectSort in case its a service in the default service implementation        
        //        if(containsFacet(BeanFacet.class)) {
        //            return ManagedObjectSort.DOMAIN_SERVICE;
        //        }
        if(isManagedBean()) {
            return BeanSort.MANAGED_BEAN;
        }
        //

        if(containsFacet(ValueFacet.class)) {
            return BeanSort.VALUE;
        }
        if(containsFacet(ViewModelFacet.class)) {
            return BeanSort.VIEW_MODEL;
        }
        if(containsFacet(MixinFacet.class)) {
            return BeanSort.MIXIN;
        }
        if(containsFacet(CollectionFacet.class)) {
            return BeanSort.COLLECTION;
        }
        if(containsFacetWithInterface(EntityFacet.class)) {
            return BeanSort.ENTITY;
        }
        val correspondingClass = getCorrespondingClass();
        if(JdoMetamodelUtil.isPersistenceEnhanced(correspondingClass)) {
            return BeanSort.ENTITY;
        }

        return BeanSort.UNKNOWN;
    }

}
