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
package org.apache.isis.viewer.restfulobjects.rendering.service.swagger;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.SwaggerSpecGenerator;

import lombok.val;

@Service
@Named("isisMetaModel.swaggerServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class SwaggerServiceDefault implements SwaggerService {

    private final SwaggerSpecGenerator swaggerSpecGenerator;
    private final String basePath;

    @Inject
    public SwaggerServiceDefault(
            final SwaggerSpecGenerator swaggerSpecGenerator,
            final RestEasyConfiguration restEasyConfiguration,
            final WebAppContextPath webAppContextPath) {
        
        this.swaggerSpecGenerator = swaggerSpecGenerator;
        
        val restfulPath = restEasyConfiguration.getJaxrs().getDefaultPath();
        val restfulBase = webAppContextPath.prependContextPath(restfulPath);
        
        this.basePath = _Strings.suffix(restfulBase, "/");
    }

    @Override
    public String generateSwaggerSpec(
            final Visibility visibility,
            final Format format) {
        return swaggerSpecGenerator.generate(basePath, visibility, format);
    }

}
