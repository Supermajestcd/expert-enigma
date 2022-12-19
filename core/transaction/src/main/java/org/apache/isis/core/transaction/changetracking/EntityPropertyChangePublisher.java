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
package org.apache.isis.core.transaction.changetracking;

import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;

/**
 * Notifies {@link org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber}s.
 *
 * @see org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber
 *
 * @since 1.x but renamed/refactored for v2 {@index}
 */
public interface EntityPropertyChangePublisher {

    /**
     * Notifies {@link org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber}s that
     * a property of an entity has changed using the
     * {@link org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber#onChanging(EntityPropertyChange)}
     * callback.
     */
    void publishChangedProperties();

}
