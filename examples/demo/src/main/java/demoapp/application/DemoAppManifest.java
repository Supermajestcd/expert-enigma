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
package demoapp.application;

import javax.inject.Singleton;

import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleRestfulObjectsViewer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.config.presets.IsisPresets;
import org.apache.isis.extensions.fixtures.IsisModuleExtFixtures;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisModuleSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisModuleSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisModuleSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisModuleSecmanRealmShiro;
import org.apache.isis.extensions.sse.IsisBootSse;
import org.apache.isis.persistence.jdo.datanucleus5.IsisModuleJdoDataNucleus5;
import org.apache.isis.webboot.springboot.IsisModuleSpringBoot;
import org.apache.isis.security.shiro.IsisModuleSecurityShiro;
import org.apache.isis.viewer.wicket.viewer.IsisModuleWicketViewer;

import demoapp.dom.DemoModule;
import demoapp.utils.LibraryPreloadingService;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@PropertySources({
    @PropertySource(IsisPresets.HsqlDbInMemory),
    @PropertySource(IsisPresets.NoTranslations),
    @PropertySource(IsisPresets.SilenceWicket),
})
@Import({
    IsisModuleSpringBoot.class,
    IsisModuleSecurityShiro.class,
    IsisModuleJdoDataNucleus5.class,
    IsisModuleWicketViewer.class,
    IsisBootSse.class, // server sent events
    IsisModuleRestfulObjectsViewer.class,

    // Security Manager Extension (secman)
    IsisModuleSecmanModel.class,
    IsisModuleSecmanRealmShiro.class,
    IsisModuleSecmanPersistenceJdo.class,
    IsisModuleSecmanEncryptionJbcrypt.class,

    IsisModuleExtFixtures.class,
    
    LibraryPreloadingService.class // just a performance enhancement

})
@ComponentScan(
        basePackageClasses= {
                DemoModule.class
        }
)
public class DemoAppManifest {

    @Bean @Singleton
    public SecurityModuleConfig securityModuleConfigBean() {
        return SecurityModuleConfig.builder()
                .adminUserName("sven")
                .adminAdditionalPackagePermission("demoapp.dom")
                .adminAdditionalPackagePermission("org.apache.isis")
                .build();
    }

    @Bean @Singleton
    public PermissionsEvaluationService permissionsEvaluationService() {
        return new PermissionsEvaluationServiceAllowBeatsVeto();
    }


}
