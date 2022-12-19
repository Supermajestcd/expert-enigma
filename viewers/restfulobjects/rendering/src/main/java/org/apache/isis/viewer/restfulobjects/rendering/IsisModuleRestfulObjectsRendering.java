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
package org.apache.isis.viewer.restfulobjects.rendering;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.viewer.restfulobjects.applib.IsisModuleViewerRestfulObjectsApplib;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceContentNegotiator;
import org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader.AcceptHeaderServiceForRest;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceOrgApacheIsisV1;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceXRoDomainType;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.SwaggerServiceDefault;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.SwaggerServiceMenu;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.ClassExcluderDefault;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.SwaggerSpecGenerator;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.TaggerDefault;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.ValuePropertyFactoryDefault;

@Configuration
@Import({
        // modules
        IsisModuleViewerRestfulObjectsApplib.class,
        IsisModuleCoreRuntime.class,

        // @Component's
        ClassExcluderDefault.class,
        SwaggerSpecGenerator.class,
        TaggerDefault.class,
        ValuePropertyFactoryDefault.class,

        
        // @Service's
        AcceptHeaderServiceForRest.class,
        ContentNegotiationServiceForRestfulObjectsV1_0.class,
        ContentNegotiationServiceOrgApacheIsisV1.class,
        ContentNegotiationServiceXRoDomainType.class,
        JsonValueEncoder.class,
        RepresentationServiceContentNegotiator.class,
        SwaggerServiceDefault.class,
        SwaggerServiceMenu.class,

})
public class IsisModuleRestfulObjectsRendering {
}
