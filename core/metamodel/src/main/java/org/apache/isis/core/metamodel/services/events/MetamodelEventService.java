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
package org.apache.isis.core.metamodel.services.events;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.applib.events.ui.LayoutUiEvent;
import org.apache.isis.applib.events.ui.TitleUiEvent;

import lombok.Builder;

/**
 * 
 * @since 2.0
 *
 */
@Service 
@Named("isisMetaModel.MetamodelEventService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Builder //for JUnit Test support
public class MetamodelEventService {

    @Inject private Event<CssClassUiEvent<Object>> cssClassUiEvents;
    @Inject private Event<IconUiEvent<Object>> iconUiEvents;
    @Inject private Event<LayoutUiEvent<Object>> layoutUiEvents;
    @Inject private Event<TitleUiEvent<Object>> titleUiEvents;

    @Inject private Event<ActionDomainEvent<?>> actionDomainEvents;
    @Inject private Event<PropertyDomainEvent<?, ?>> propertyDomainEvents;
    @Inject private Event<CollectionDomainEvent<?, ?>> collectionDomainEvents;

    // -- METAMODEL UI EVENTS

    public void fireCssClassUiEvent(CssClassUiEvent<Object> event) {
        cssClassUiEvents.fire(event);
    }

    public void fireIconUiEvent(IconUiEvent<Object> event) {
        iconUiEvents.fire(event);
    }

    public void fireLayoutUiEvent(LayoutUiEvent<Object> event) {
        layoutUiEvents.fire(event);
    }

    public void fireTitleUiEvent(TitleUiEvent<Object> event) {
        titleUiEvents.fire(event);
    }

    public void fireActionDomainEvent(ActionDomainEvent<?> event) {
        actionDomainEvents.fire(event);
    }

    public void firePropertyDomainEvent(PropertyDomainEvent<?, ?> event) {
        propertyDomainEvents.fire(event);
    }

    public void fireCollectionDomainEvent(CollectionDomainEvent<?, ?> event) {
        collectionDomainEvents.fire(event);
    }


}
