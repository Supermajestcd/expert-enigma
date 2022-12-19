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
package org.apache.isis.testing.unittestsupport.applib.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.apache.isis.commons.internal.base._NullSafe.stream;


public class SoapEndpointPublishingRule implements TestRule {

    public SoapEndpointPublishingRule(final Class<?> endpointClass, final String endpointAddress) {
        SoapPublishedEndpoints.instance().publishIfRequired(
                new SoapEndpointSpec(endpointClass, endpointAddress));
    }

    public SoapEndpointPublishingRule(Class<?>... endpointClasses) {
        SoapPublishedEndpoints.instance().publishIfRequired(
                stream(endpointClasses)
                    .map(SoapEndpointSpec::asSoapEndpointSpec)
                    .collect(Collectors.toCollection(ArrayList::new)));
    }

    public SoapEndpointPublishingRule(final List<Class<?>> endpointClasses) {
        SoapPublishedEndpoints.instance().publishIfRequired(
                stream(endpointClasses)
                .map(SoapEndpointSpec::asSoapEndpointSpec)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public SoapEndpointPublishingRule(SoapEndpointSpec... soapEndpointSpecs) {
        SoapPublishedEndpoints.instance().publishIfRequired(
                stream(soapEndpointSpecs)
                    .collect(Collectors.toCollection(ArrayList::new)));
    }

    public SoapEndpointPublishingRule(final Iterable<SoapEndpointSpec> soapEndpointSpecs) {
        SoapPublishedEndpoints.instance().publishIfRequired(
                stream(soapEndpointSpecs)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        // now a no-op
        return base;
    }

    public String getEndpointAddress(Class<?> endpointClass) {
        return SoapPublishedEndpoints.instance().getEndpointAddress(endpointClass);
    }

    public <T> T getEndpointImplementor(Class<T> endpointClass) {
        return SoapPublishedEndpoints.instance().getEndpointImplementor(endpointClass);
    }

}
