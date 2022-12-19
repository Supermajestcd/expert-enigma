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
package demoapp.dom.error.service;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.error.ErrorDetails;
import org.apache.isis.applib.services.error.ErrorReportingService;
import org.apache.isis.applib.services.error.Ticket;
import org.apache.isis.applib.services.error.Ticket.StackTracePolicy;
import org.apache.isis.core.runtimeservices.error.EmailTicket;
import org.apache.isis.core.runtimeservices.error.EmailTicket.MailTo;

import lombok.val;

// tag::refguide[]
@Service
@Named("demoapp.demoErrorReportingService")
@Qualifier("demo")
public class DemoErrorReportingService implements ErrorReportingService {

    @Override
    public Ticket reportError(ErrorDetails errorDetails) {

        String reference = "#0";
        String userMessage = errorDetails.getMainMessage();
        String details = "Apologies!";

        val mailTo = MailTo.builder()
                .receiver("support@hello.world")
                .subject("[Demo-App] Unexpected Error (" + reference + ")")
                .body(MailTo.mailBodyOf(errorDetails))
                .build();

        StackTracePolicy policy = StackTracePolicy.SHOW;
        val ticket = new EmailTicket(mailTo, reference, userMessage, details,
                policy,
                "http://www.randomkittengenerator.com/cats/rotator.php");

        return ticket;
    }
}
// end::refguide[]
