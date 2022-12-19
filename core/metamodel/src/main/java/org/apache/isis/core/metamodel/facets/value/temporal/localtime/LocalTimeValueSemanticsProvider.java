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

package org.apache.isis.core.metamodel.facets.value.temporal.localtime;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalAdjust;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueSemanticsProviderAbstract;

import lombok.val;

//@Log4j2
public class LocalTimeValueSemanticsProvider
extends TemporalValueSemanticsProviderAbstract<LocalTime> {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

    public LocalTimeValueSemanticsProvider(final FacetHolder holder) {
        super(TemporalValueFacet.class,
                TemporalCharacteristic.TIME_ONLY, OffsetCharacteristic.LOCAL,
                holder, LocalTime.class, TYPICAL_LENGTH, MAX_LENGTH,
                LocalTime::from,
                TemporalAdjust::adjustLocalTime);
        
        val hourMinuteSecondMillis = "HH:mm:ss.SSS";
        val basicTimeNoMillis = "HHmmssZ";
        val basicTime = "HHmmss.SSSZ";
        
        super.addNamedFormat("iso", basicTimeNoMillis);
        super.addNamedFormat("iso_encoding", basicTime);
        super.updateParsers();

        setEncodingFormatter(DateTimeFormatter.ofPattern(hourMinuteSecondMillis, Locale.getDefault()));
        
        val configuredNameOrPattern = getConfiguration().getValueTypes().getJavaTime().getLocalTime().getFormat();

        // walk through 3 methods of generating a formatter, first one to return non empty wins
        val formatter = formatterFirstOf(Can.of(
                ()->lookupFormatStyle(configuredNameOrPattern).map(DateTimeFormatter::ofLocalizedTime),
                ()->lookupNamedFormatter(configuredNameOrPattern),
                ()->formatterFromPattern(configuredNameOrPattern)
                ))
        .orElseGet(()->DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));  // fallback
        
        setTitleFormatter(formatter);
        
    }

}
