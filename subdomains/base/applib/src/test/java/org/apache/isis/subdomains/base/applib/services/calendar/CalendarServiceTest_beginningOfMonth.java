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
package org.apache.isis.subdomains.base.applib.services.calendar;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.services.clock.ClockService;

@RunWith(Parameterized.class)
public class CalendarServiceTest_beginningOfMonth {

    private CalendarService calendarService;
    private ClockService stubClockService;
    protected LocalDate now;
    private LocalDate expected;

    @Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(
              new Object[][] { 
                      { LocalDate.of(2013,4,15), LocalDate.of(2013,4,1)}, 
                      { LocalDate.of(2013,4,1),  LocalDate.of(2013,4,1)}, 
                      { LocalDate.of(2013,4,30),  LocalDate.of(2013,4,1)}, 
              });
    }
    
    public CalendarServiceTest_beginningOfMonth(LocalDate date, LocalDate expected) {
        this.now = date;
        this.expected = expected;
    }
    
    @Before
    public void setUp() throws Exception {

        stubClockService = new ClockService() {
            @Override
            public LocalDate now() {
                return now;
            }
        };

        calendarService = new CalendarService(stubClockService);
        
    }
    
    @Test
    public void test() throws Exception {
        assertThat(calendarService.beginningOfMonth(), is(expected));
    }


}
