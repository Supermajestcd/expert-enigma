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

package org.apache.isis.applib.services.wrapper;

import java.util.EnumSet;
import java.util.List;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;

import static org.apache.isis.core.commons.collections.ImmutableEnumSet.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * Provides the ability to &quot;wrap&quot; of a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implied by
 * the Isis programming model.
 *
 * <p>
 * The 'wrap' is a runtime-code-generated proxy that wraps the underlying domain
 * object. The wrapper can then be interacted with as follows:
 * <ul>
 * <li>a <tt>get</tt> method for properties or collections</li>
 * <li>a <tt>set</tt> method for properties</li>
 * <li>an <tt>addTo</tt> or <tt>removeFrom</tt> method for collections</li>
 * <li>any action</li>
 * </ul>
 *
 * <p>
 * Calling any of the above methods may result in a (subclass of)
 * {@link InteractionException} if the object disallows it. For example, if a
 * property is annotated with {@link Hidden} then a {@link HiddenException} will
 * be thrown. Similarly if an action has a <tt>validate</tt> method and the
 * supplied arguments are invalid then a {@link InvalidException} will be
 * thrown.
 *
 * <p>
 * In addition, the following methods may also be called:
 * <ul>
 * <li>the <tt>title</tt> method</li>
 * <li>any <tt>defaultXxx</tt> or <tt>choicesXxx</tt> method</li>
 * </ul>
 *
 * <p>
 * An exception will be thrown if any other methods are thrown.
 *
 * <p>
 * An implementation of this service (<tt>WrapperFactoryDefault</tt>) can be registered by including
 * <tt>o.a.i.core:isis-core-wrapper</tt> on the classpath; no further configuration is required.
 * </p>
 */
// tag::refguide[]
// tag::refguide-async[]
// tag::refguide-listeners[]
public interface WrapperFactory {

    // end::refguide-listeners[]
    // end::refguide-async[]
    // end::refguide[]
    /**
     * Whether interactions with the wrapper are actually passed onto the
     * underlying domain object.
     *
     * @see WrapperFactory#wrap(Object, ImmutableEnumSet)
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    // tag::refguide-1[]
    enum ExecutionMode {
        // end::refguide-1[]
        /**
         * Skip all business rules.
         */
        // tag::refguide-1[]
        SKIP_RULE_VALIDATION,
        // end::refguide-1[]
        /**
         * Skip execution.
         */
        // tag::refguide-1[]
        SKIP_EXECUTION,
        // end::refguide-1[]
        /**
         * Don't fail fast, swallow any exception during validation or execution.
         */
        // tag::refguide-1[]
        SWALLOW_EXCEPTIONS,
    }
    // end::refguide-1[]

    // tag::refguide-2[]
    @UtilityClass
    class ExecutionModes {
        // end::refguide-2[]
        /**
         * Validate all business rules and then execute. May throw exceptions in order to fail fast.
         */
        // tag::refguide-2[]
        public static final ImmutableEnumSet<ExecutionMode> EXECUTE =
                                noneOf(ExecutionMode.class);
        // end::refguide-2[]
        /**
         * Skip all business rules and then execute, does throw an exception if execution fails.
         */
        // tag::refguide-2[]
        public static final ImmutableEnumSet<ExecutionMode> SKIP_RULES =
                                of(ExecutionMode.SKIP_RULE_VALIDATION);
        // end::refguide-2[]
        /**
         * Validate all business rules but do not execute, throw an exception if validation
         * fails.
         */
        // tag::refguide-2[]
        public static final ImmutableEnumSet<ExecutionMode> NO_EXECUTE =
                                of(ExecutionMode.SKIP_EXECUTION);
        // end::refguide-2[]
        /**
         * Validate all business rules and then execute, but don't throw an exception if validation
         * or execution fails.
         */
        // tag::refguide-2[]
        public static final ImmutableEnumSet<ExecutionMode> TRY =
                                of(ExecutionMode.SWALLOW_EXCEPTIONS);
        // end::refguide-2[]
        /**
         * Skips all steps.
         * @since 2.0
         */
        // tag::refguide-2[]
        public static final ImmutableEnumSet<ExecutionMode> NOOP =
                                of(ExecutionMode.SKIP_RULE_VALIDATION,
                                   ExecutionMode.SKIP_EXECUTION);
    }
    // end::refguide-2[]

    /**
     * Same as {@link #wrap(Object)}, except the actual execution occurs only if
     * the <tt>execute</tt> parameter indicates.
     *
     * <p>
     * Otherwise, will do all the validations (raise exceptions as required
     * etc.), but doesn't modify the model.
     */
    // tag::refguide[]
    <T> T wrap(T domainObject,                                      // <.>
               ImmutableEnumSet<ExecutionMode> mode);

    // end::refguide[]
    /**
     * Provides the &quot;wrapper&quot; of the underlying domain object.
     *
     * <p>
     * If the object has (see {@link #isWrapper(Object)} already been wrapped),
     * then should just return the object back unchanged.
     */
    // tag::refguide[]
    <T> T wrap(T domainObject);                                     // <.>

    // end::refguide[]
    /**
     * Convenience method for {@link #wrap(Object, ImmutableEnumSet)} with {@link ExecutionModes#TRY},
     * to make this feature more discoverable.
     */
    // tag::refguide[]
    <T> T wrapTry(T domainObject);                                  // <.>

    // end::refguide[]
    /**
     * Convenience method for {@link #wrap(Object, ImmutableEnumSet)} with {@link ExecutionModes#NO_EXECUTE},
     * to make this feature more discoverable.
     */
    // tag::refguide[]
    <T> T wrapNoExecute(T domainObject);                            // <.>

    // end::refguide[]
    /**
     * Convenience method for {@link #wrap(Object, ImmutableEnumSet)} with {@link ExecutionModes#SKIP_RULES},
     * to make this feature more discoverable.
     */
    // tag::refguide[]
    <T> T wrapSkipRules(T domainObject);                            // <.>

    // end::refguide[]
    /**
     * {@link #wrap(Object) wraps} a {@link FactoryService#mixin(Class, Object) mixin}.
     */
    // tag::refguide[]
    <T> T wrapMixin(Class<T> mixinClass, Object mixedIn);           // <.>

    // end::refguide[]
    /**
     * Obtains the underlying domain object, if wrapped.
     *
     * <p>
     * If the object {@link #isWrapper(Object) is not wrapped}, then
     * should just return the object back unchanged.
     */
    // tag::refguide[]
    <T> T unwrap(T possibleWrappedDomainObject);                    // <.>

    // end::refguide[]
    /**
     * Whether the supplied object has been wrapped.
     *
     * @param <T>
     * @param possibleWrappedDomainObject
     *            - object that might or might not be a wrapper.
     * @return
     */
    // tag::refguide[]
    <T> boolean isWrapper(T possibleWrappedDomainObject);           // <.>

    // end::refguide[]


    //
    // -- ASYNC WRAPPING
    //

    /**
     * Returns a {@link AsyncWrap} bound to the provided {@code domainObject},
     * to prepare for type-safe asynchronous action execution.
     *
     * @param <T>
     * @param domainObject
     * @param mode
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    <T> AsyncWrap<T> async(T domainObject,                          // <.>
                           ImmutableEnumSet<ExecutionMode> mode);

    // end::refguide-async[]
    /**
     * Shortcut for {@link #async(Object, ImmutableEnumSet)} using execution mode
     * {@link ExecutionModes#EXECUTE}.
     * @param <T>
     * @param domainObject
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    default <T> AsyncWrap<T> async(T domainObject) {                // <.>
        // end::refguide-async[]

        return async(domainObject, ExecutionModes.EXECUTE);

        // tag::refguide-async[]
        // ...
    }

    // end::refguide-async[]
    /**
     * Returns a {@link AsyncWrap} bound to the provided {@code mixinClass},
     * to prepare for type-safe asynchronous action execution.
     *
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @param mode
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    <T> AsyncWrap<T> asyncMixin(                                    // <.>
                        Class<T> mixinClass, Object mixedIn,
                        ImmutableEnumSet<ExecutionMode> mode);

    // end::refguide-async[]
    /**
     * Shortcut for {@link #asyncMixin(Class, Object, ImmutableEnumSet)} using execution mode
     * {@link ExecutionModes#EXECUTE}.
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    default <T> AsyncWrap<T> asyncMixin(                            // <.>
                        Class<T> mixinClass, Object mixedIn) {
        // end::refguide-async[]

        return asyncMixin(mixinClass, mixedIn, ExecutionModes.EXECUTE);

        // tag::refguide-async[]
        // ...
    }
    // end::refguide-async[]


    //
    // -- ITERACTION EVENT HANDLING
    //

    /**
     * All {@link InteractionListener}s that have been registered using
     * {@link #addInteractionListener(InteractionListener)}.
     */
    // tag::refguide-listeners[]
    // ...
    List<InteractionListener> getListeners();                       // <.>

    // end::refguide-listeners[]
    /**
     * Registers an {@link InteractionListener}, to be notified of interactions
     * on all wrappers.
     *
     * <p>
     * This is retrospective: the listener will be notified of interactions even
     * on wrappers created before the listener was installed. (From an
     * implementation perspective this is because the wrappers delegate back to
     * the container to fire the events).
     *
     * @param listener
     * @return
     */
    // tag::refguide-listeners[]
    boolean addInteractionListener(InteractionListener listener);   // <.>

    // end::refguide-listeners[]
    /**
     * Remove an {@link InteractionListener}, to no longer be notified of
     * interactions on wrappers.
     *
     * <p>
     * This is retrospective: the listener will no longer be notified of any
     * interactions created on any wrappers, not just on those wrappers created
     * subsequently. (From an implementation perspective this is because the
     * wrappers delegate back to the container to fire the events).
     *
     * @param listener
     * @return
     */
    // tag::refguide-listeners[]
    boolean removeInteractionListener(                              // <.>
                    InteractionListener listener);

    void notifyListeners(InteractionEvent ev);                      // <.>
    // tag::refguide-async[]
    // tag::refguide[]
    // ...

}
// end::refguide[]
// end::refguide-listeners[]
// end::refguide-async[]
