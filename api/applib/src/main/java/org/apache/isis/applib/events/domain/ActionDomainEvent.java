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
package org.apache.isis.applib.events.domain;

import java.util.List;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

import lombok.Getter;
import lombok.Setter;

/**
 * @since 1.x {@index}
 */
public abstract class ActionDomainEvent<S> extends AbstractDomainEvent<S> {

    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.Action#domainEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the 
     * <tt>isis.core.meta-model.annotation.action.domain-event.post-for-default</tt>
     * configuration property.
     */
    public static class Default extends ActionDomainEvent<Object> {}

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted 
     * (irrespective of the configuration property setting for the {@link Default} event.
     */
    public static class Noop extends ActionDomainEvent<Object> {}

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends ActionDomainEvent<Object> {}

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Recommended because it reduces the amount of boilerplate in the domain object classes.
     * </p>
     */
    public ActionDomainEvent() {
    }

    @Getter
    private SemanticsOf semantics;

    @Getter
    private List<String> parameterNames;

    @Getter
    private List<Class<?>> parameterTypes;

    /**
     * Populated only for mixins; holds the underlying domain object that the mixin contributes to.
     */
    @Getter
    private Object mixedIn;

    /**
     * The arguments being used to invoke the action;
     * populated at {@link AbstractDomainEvent.Phase#VALIDATE} and subsequent phases
     * (but null for {@link AbstractDomainEvent.Phase#HIDE hidden} and 
     * {@link AbstractDomainEvent.Phase#DISABLE disable} phases).
     *
     * <p>
     *     The argument values can also be modified by event handlers
     *     during the {@link AbstractDomainEvent.Phase#EXECUTING} phase. The new value must be
     *     the same type as the expected value; the framework performs
     *     no sanity checks.
     * </p>
     */
    @Getter @Setter
    private List<Object> arguments;

    /**
     * The value returned by the action.
     *
     * <p>
     * Only available for the {@link AbstractDomainEvent.Phase#EXECUTED}
     * {@link #getEventPhase() phase}.
     * </p>
     */
    @Getter
    private Object returnValue;

    /**
     * Set by the framework.
     *
     * Event subscribers can replace the value with some other value if they wish, though only in the
     * {@link AbstractDomainEvent.Phase#EXECUTED} phase.
     */
    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * Not API - set by the framework.
     */
    public void setSemantics(SemanticsOf semantics) {
        this.semantics = semantics;
    }
    /**
     * Not API - set by the framework.
     */
    public void setParameterNames(final List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }
    /**
     * Not API - set by the framework.
     */
    public void setParameterTypes(final List<Class<?>> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    /**
     * Not API - set by the framework.
     */
    @Override
    public void setMixedIn(final Object mixedIn) {
        this.mixedIn = mixedIn;
    }


    private static final ToString<ActionDomainEvent<?>> toString = ObjectContracts.<ActionDomainEvent<?>>
    toString("source", ActionDomainEvent::getSource)
    .thenToString("identifier", ActionDomainEvent::getIdentifier)
    .thenToString("eventPhase", ActionDomainEvent::getEventPhase)
    ;

    @Override
    public String toString() {
        return toString.toString(this);
    }


}
