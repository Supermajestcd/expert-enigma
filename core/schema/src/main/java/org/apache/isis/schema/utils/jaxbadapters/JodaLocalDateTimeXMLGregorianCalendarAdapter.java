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
package org.apache.isis.schema.utils.jaxbadapters;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import org.joda.time.LocalDateTime;

/**
 * Note: not actually registered as a JAXB adapter.
 */
public final class JodaLocalDateTimeXMLGregorianCalendarAdapter {
    private JodaLocalDateTimeXMLGregorianCalendarAdapter() {
    }

    public static LocalDateTime parse(final XMLGregorianCalendar xgc) {
        if(xgc == null) return null;

        final int year = xgc.getYear();
        final int month = xgc.getMonth();
        final int day = xgc.getDay();
        final int hour = xgc.getHour();
        final int minute = xgc.getMinute();
        final int second = xgc.getSecond();
        final int millisecond = xgc.getMillisecond();

        return new LocalDateTime(year, month, day, hour, minute, second, millisecond);
    }

    public static XMLGregorianCalendar print(final LocalDateTime dateTime) {
        if(dateTime == null) {
            return null;
        }

        final XMLGregorianCalendarImpl xgc = new XMLGregorianCalendarImpl();
        xgc.setYear(dateTime.getYear());
        xgc.setMonth(dateTime.getMonthOfYear());
        xgc.setDay(dateTime.getDayOfMonth());
        xgc.setHour(dateTime.getHourOfDay());
        xgc.setMinute(dateTime.getMinuteOfHour());
        xgc.setSecond(dateTime.getSecondOfMinute());
        xgc.setMillisecond(dateTime.getMillisOfSecond());

        return xgc;
    }

}