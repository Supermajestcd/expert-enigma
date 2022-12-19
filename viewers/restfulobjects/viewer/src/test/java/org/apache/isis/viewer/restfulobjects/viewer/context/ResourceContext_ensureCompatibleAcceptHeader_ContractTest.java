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
package org.apache.isis.viewer.restfulobjects.viewer.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.session.IsisSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.session.IsisSessionTracker;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ResourceDescriptor;

import lombok.val;

public abstract class ResourceContext_ensureCompatibleAcceptHeader_ContractTest {

    @Rule public JUnitRuleMockery2 context = 
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock HttpHeaders mockHttpHeaders;
    @Mock HttpServletRequest mockHttpServletRequest;
    @Mock ServletContext mockServletContext;
    @Mock IsisSessionFactory mockIsisSessionFactory;
    @Mock IsisSession mockIsisSession;
    @Mock AuthenticationSession mockAuthenticationSession;
    @Mock SpecificationLoader mockSpecificationLoader;
    @Mock WebApplicationContext webApplicationContext;
    @Mock IsisSessionTracker mockIsisSessionTracker;
    @Mock AuthenticationManager mockAuthenticationManager;
    
    MetaModelContext metaModelContext;

    @Before
    public void setUp() throws Exception {

        // PRODUCTION

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .authenticationSession(mockAuthenticationSession)
                .singleton(mockAuthenticationManager)
                .singleton(mockIsisSessionTracker)
                .singleton(mockIsisSessionFactory)
                .build();

        context.checking(new Expectations() {
            {
                
                allowing(mockServletContext).getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
                will(returnValue(webApplicationContext));
                
                allowing(webApplicationContext).getBean(MetaModelContext.class);
                will(returnValue(metaModelContext));
                
                allowing(mockHttpServletRequest).getServletContext();
                will(returnValue(mockServletContext));
                
                allowing(mockHttpServletRequest).getQueryString();
                will(returnValue(""));
                
//                allowing(mockIsisSession).getAuthenticationSession();
//                will(returnValue(mockAuthenticationSession));
                
            }
        });
    }

    @Test
    public void noop() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(representationType.getMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test
    public void happyCase() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(representationType.getMediaType()));

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.GENERIC;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test
    public void acceptGenericAndProduceSpecific() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_JSON_TYPE));

        instantiateResourceContext(representationType);
    }

    @Test
    public void nonMatching() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(MediaType.APPLICATION_ATOM_XML_TYPE));

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void nonMatchingProfile() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(RepresentationType.USER.getMediaType()));

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void nonMatchingProfile_ignoreGeneric() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList(RepresentationType.USER.getMediaType(), MediaType.APPLICATION_JSON_TYPE));

        try {
            instantiateResourceContext(representationType);
        } catch(RestfulObjectsApplicationException ex ) {
            assertThat(ex.getHttpStatusCode(), is(HttpStatusCode.NOT_ACCEPTABLE));
        }
    }

    @Test
    public void emptyList_isOK() throws Exception {
        final RepresentationType representationType = RepresentationType.HOME_PAGE;
        givenHttpHeadersGetAcceptableMediaTypesReturns(Arrays.<MediaType> asList());

        instantiateResourceContext(representationType);
    }

    private void givenHttpHeadersGetAcceptableMediaTypesReturns(final List<MediaType> mediaTypes) {
        context.checking(new Expectations() {
            {
                allowing(mockHttpHeaders).getAcceptableMediaTypes();
                will(returnValue(mediaTypes));
            }
        });
    }

    private void givenServletRequestParameterMapEmpty() {
        final HashMap<Object, Object> parameterMap = _Maps.newHashMap();
        context.checking(new Expectations() {
            {
                allowing(mockHttpServletRequest).getParameterMap();
                will(returnValue(parameterMap));
            }
        });
    }

    private ResourceContext instantiateResourceContext(
            final RepresentationType representationType) {
        
        val resourceDescriptor = ResourceDescriptor.of(representationType, null, null);
        
        return new ResourceContext(resourceDescriptor, mockHttpHeaders, null, null, null, null, 
                mockHttpServletRequest, null, null,
                metaModelContext, null, null);
    }

}
