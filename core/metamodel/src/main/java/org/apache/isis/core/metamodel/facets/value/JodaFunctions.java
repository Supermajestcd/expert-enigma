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

package org.apache.isis.core.metamodel.facets.value;

import java.util.Locale;

import com.google.common.base.Function;

import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.applib.profiles.Localization;

public final class JodaFunctions  {

    private JodaFunctions(){}
    
    public static Function<DateTimeFormatter, DateTimeFormatter> withLocale(final Localization localization) {
        return new Function<DateTimeFormatter, DateTimeFormatter>() {
            @Override
            public DateTimeFormatter apply(DateTimeFormatter input) {
                if (localization == null) {
                    return input;
                }
                final Locale locale = localization.getLocale();
                if (locale == null) {
                    return input;
                }
                return input.withLocale(locale);
            }
        };
    }

}