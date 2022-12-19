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
package org.apache.isis.applib.events.lifecycle;

// tag::refguide[]
public abstract class ObjectCreatedEvent<S> extends AbstractLifecycleEvent<S> {

    // end::refguide[]
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.DomainObject#createdLifecycleEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the <tt>isis.core.meta-model.annotation.domain-object.created-lifecycle-event.post-for-default</tt>
     * configuration property.
     */
    // tag::refguide[]
    public static class Default extends ObjectCreatedEvent<Object> {}

    // end::refguide[]
    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    // tag::refguide[]
    public static class Noop extends ObjectCreatedEvent<Object> {}

    // end::refguide[]
    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    // tag::refguide[]
    public static class Doop extends ObjectCreatedEvent<Object> {}

    // end::refguide[]
    public ObjectCreatedEvent() {
    }

    public ObjectCreatedEvent(final S source) {
        super(source);
    }

    // tag::refguide[]

}
// end::refguide[]
