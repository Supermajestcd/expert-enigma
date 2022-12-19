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
package org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.services.acceptheader.AcceptHeaderService;
import org.apache.isis.core.commons.internal.base._NullSafe;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRoRendering.AcceptHeaderServiceForRest")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("ForRest")
@IsisInteractionScope
@Log4j2
public class AcceptHeaderServiceForRest implements AcceptHeaderService {

    private static ThreadLocal<List<MediaType>> mediaTypesByThread = new ThreadLocal<>();

    /**
     * Not API - called by RO viewer filter.
     */
    private static void setMediaTypes(List<MediaType> mediaTypes) {
        mediaTypesByThread.set(mediaTypes);
    }
    /**
     * Not API - called by RO viewer filter.
     */
    private static void removeMediaTypes() {
        mediaTypesByThread.remove();
    }


    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return mediaTypesByThread.get();
    }


    @Component
    @Provider
    public static class RequestFilter implements ContainerRequestFilter  {
        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            final List<MediaType> acceptableMediaTypes = requestContext.getAcceptableMediaTypes();

            final List<MediaType> mediaTypes = stream(acceptableMediaTypes)
                    .filter(_NullSafe::isPresent)
                    .collect(Collectors.toList());

            setMediaTypes(mediaTypes);
        }
    }

    @Component
    @Provider
    public static class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(
                final ContainerRequestContext requestContext, 
                final ContainerResponseContext responseContext) throws IOException {

            removeMediaTypes();
        }
    }

}
