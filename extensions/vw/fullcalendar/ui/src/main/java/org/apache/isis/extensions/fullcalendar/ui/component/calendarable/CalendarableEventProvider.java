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
package org.apache.isis.extensions.fullcalendar.ui.component.calendarable;

import java.util.Map;

import com.google.common.base.Function;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.fullcalendar.applib.CalendarEventable;
import org.apache.isis.extensions.fullcalendar.applib.Calendarable;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.extensions.fullcalendar.ui.component.EventProviderAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

public class CalendarableEventProvider extends EventProviderAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarableEventProvider(
            final EntityCollectionModel collectionModel,
            final String calendarName) {
        super(collectionModel, calendarName);
    }

    @Override
    protected CalendarEvent calendarEventFor(
            final Object domainObject,
            final String calendarName) {
        if(!(domainObject instanceof Calendarable)) {
            return null;
        }
        final Calendarable calendarable = (Calendarable)domainObject;
        final Map<String, CalendarEventable> calendarEvents = calendarable.getCalendarEvents();
        final CalendarEventable calendarEventable = calendarEvents.get(calendarName);
        return calendarEventable!=null
                ?calendarEventable.toCalendarEvent()
                :null;
    }

    static final Function<ManagedObject, Iterable<String>> GET_CALENDAR_NAMES =
            input -> {
                final Object domainObject = input.getPojo();
                if(!(domainObject instanceof Calendarable)) {
                    return null;
                }
                final Calendarable calendarable = (Calendarable) domainObject;
                return calendarable.getCalendarNames();
            };


}
