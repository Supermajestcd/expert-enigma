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
package org.apache.isis.applib.services.metrics;

import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;

// tag::refguide[]
public interface MetricsService {

    // end::refguide[]
    /**
     * The number of objects that have, so far in this request, been loaded from the database.
     *
     * <p>
     *     Corresponds to the number of times that <code>javax.jdo.listener.LoadLifecycleListener#postLoad(InstanceLifecycleEvent)</code> (or equivalent) is fired.
     * </p>
     *
     * <p>
     *     Is captured within {@link MemberExecutionDto#getMetrics()} (accessible from {@link InteractionContext#getInteraction()}).
     * </p>
     */
    // tag::refguide[]
    int numberObjectsLoaded();      // <.>

    // end::refguide[]
    /**
     * The number of objects that have, so far in this request, been dirtied/will need updating in the database); a
     * good measure of the footprint of the interaction.
     *
     * <p>
     *     Corresponds to the number of times that <code>javax.jdo.listener.DirtyLifecycleListener#preDirty(InstanceLifecycleEvent)</code> (or equivalent) callback is fired.
     * </p>
     *
     * <p>
     *     Is captured within {@link MemberExecutionDto#getMetrics()} (accessible from {@link InteractionContext#getInteraction()}).
     * </p>
     */
    // tag::refguide[]
    int numberObjectsDirtied();     // <.>

}
// end::refguide[]


