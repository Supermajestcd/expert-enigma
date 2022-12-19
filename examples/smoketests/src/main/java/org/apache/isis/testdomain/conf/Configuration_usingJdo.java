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
package org.apache.isis.testdomain.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.config.IsisPresets;
import org.apache.isis.extensions.fixtures.IsisExtFixturesModule;
import org.apache.isis.jdo.IsisBootDataNucleus;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.bypass.IsisBootSecurityBypass;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;

@Configuration
@Import({
    IsisBoot.class,
    IsisBootSecurityBypass.class,
    IsisBootDataNucleus.class,
    IsisExtFixturesModule.class
})
@ComponentScan(
        basePackageClasses= {               
                JdoTestDomainModule.class
        })
@PropertySources({
    @PropertySource("classpath:/org/apache/isis/testdomain/jdo/isis-persistence.properties"),
    @PropertySource(IsisPresets.H2InMemory_withUniqueSchema),
    @PropertySource(IsisPresets.NoTranslations),
})
public class Configuration_usingJdo {
    

}