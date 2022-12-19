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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

/**
 * Domain semantics for domain object collection.
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE }
)
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "act") // meta annotation, only applies at class level
public @interface Action {

    /**
     * Associates this action with a property or collection, specifying its id.
     *
     * <p>
     *     This is an alternative to using {@link MemberOrder#name()}.  To specify the order (equivalent to
     *     {@link MemberOrder#sequence()}}), use {@link #associateWithSequence()}.
     * </p>
     *
     * <p>
     *     For example <code>@Action(associateWith="items", associateWithSequence="2.1")</code>
     * </p>
     *
     * <p>
     *     If an action is associated with a collection, then any matching parameters will have
     *     their choices automatically inferred from the collection (if not otherwise specified)
     *     and any collection parameter defaults can be specified using checkboxes
     *     (in the Wicket UI, at least).
     * </p>
     */
    String associateWith()
            default "";

    /**
     * Specifies the sequence/order in the UI for an action that's been associated with a property or collection.
     *
     * <p>
     *     This is an alternative to using {@link MemberOrder#sequence()}, but is ignored if
     *     {@link Action#associateWith()} isn't also specified.
     * </p>
     *
     * <p>
     *     For example <code>@Action(associateWith="items", associateWithSequence="2.1")</code>
     * </p>
     */
    String associateWithSequence()
            default "1";

    /**
     * The {@link CommandDtoProcessor} to process this command's DTO.
     *
     * <p>
     *     The processor itself is used by {@link ContentMappingServiceForCommandDto} and
     *     {@link ContentMappingServiceForCommandsDto} to dynamically transform the DTOs.
     * </p>
     */
    Class<? extends CommandDtoProcessor> commandDtoProcessor()
            default CommandDtoProcessor.class;

    /**
     * Whether action invocations, captured as {@link Command}s,
     * should be published to {@link CommandSubscriber}s.
     */
    Publishing commandPublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * Indicates that an invocation of the action should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.domain.ActionDomainEvent}.
     *
     * <p>For example:
     * </p>
     *
     * <pre>
     * public class SomeObject{
     *     public static class ChangeStartDateDomainEvent extends ActionDomainEvent&lt;SomeObject&gt; { ... }
     *
     *     &#64;Action(domainEvent=ChangedStartDateDomainEvent.class)
     *     public void changeStartDate(final Date startDate) { ...}
     *     ...
     * }
     * </pre>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ActionDomainEvent<?>> domainEvent()
            default ActionDomainEvent.Default.class;

    /**
     * Whether {@link Execution}s (triggered by action invocations), should
     * be published to {@link ExecutionSubscriber}s.
     */
    Publishing executionPublishing()
            default Publishing.NOT_SPECIFIED;
    
    /**
     * Indicates where (in the UI) the action is not visible to the user.
     *
     * <p>
     * It is also possible to suppress an action's visibility using {@link ActionLayout#hidden()}.
     *
     * <p>
     * For {@link DomainService domain service} actions, the action's visibility is dependent upon its
     * {@link DomainService#nature() nature}.
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * Whether the action is restricted to prototyping.
     *
     * <p>
     *     By default there are no restrictions, with the action being available in all environments.
     * </p>
     */
    RestrictTo restrictTo()
            default RestrictTo.NOT_SPECIFIED;

    /**
     * The action semantics, either {@link SemanticsOf#SAFE_AND_REQUEST_CACHEABLE cached}, {@link SemanticsOf#SAFE safe} (query-only),
     * {@link SemanticsOf#IDEMPOTENT idempotent} or
     * {@link SemanticsOf#NON_IDEMPOTENT non-idempotent}.
     */
    SemanticsOf semantics()
            default SemanticsOf.NOT_SPECIFIED;

    /**
     * The type-of the elements returned by the action.
     */
    Class<?> typeOf()
            default Object.class;

    /**
     * For downloading {@link Blob} or {@link Clob}, optionally restrict the files accepted (eg <tt>.xslx</tt>).
     *
     * <p>
     * The value should be of the form "file_extension|audio/*|video/*|image/*|media_type".
     * </p>
     *
     * @see <a href="http://www.w3schools.com/tags/att_input_accept.asp">http://www.w3schools.com</a>
     *
     */
    String fileAccept()
            default "";

}
