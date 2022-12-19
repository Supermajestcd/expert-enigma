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
package org.apache.isis.extensions.secman.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.model.facets.TenantedAuthorizationPostProcessor;
import org.apache.isis.extensions.secman.model.seed.SeedSecurityModuleService;
import org.apache.isis.extensions.secman.model.spiimpl.ImpersonateMenuAdvisorForSecman;
import org.apache.isis.extensions.secman.model.spiimpl.TableColumnVisibilityServiceForSecman;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // Module
        IsisModuleExtSecmanApi.class,

        // @Component
        TenantedAuthorizationPostProcessor.Register.class,
        TableColumnVisibilityServiceForSecman.class,
        ImpersonateMenuAdvisorForSecman.class, //not activated by default yet

        SeedSecurityModuleService.class,

})
public class IsisModuleExtSecmanModel {

}
