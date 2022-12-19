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
package org.apache.isis.config;

import javax.inject.Singleton;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.config.internal._Config;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Configuration
@Import({
    //IsisConfiguration.class // not required
    IsisConfiguration.PatternsConverter.class,
    IsisSystemEnvironment.class
})
@EnableConfigurationProperties(IsisConfiguration.class)
@Log4j2
public class IsisConfigModule {
    
    
    /** @deprecated this is just a historical workaround */
    @Bean @Singleton
    public IsisConfigurationLegacy getConfigurationLegacy(
            ConfigurableEnvironment configurableEnvironment,
            IsisSystemEnvironment isisSystemEnvironment) {
        
        _Config.clear();
        
        val rawKeyValueMap = isisSystemEnvironment.getIocContainer()
                .copyEnvironmentToMap(configurableEnvironment);
        _Config.putAll(rawKeyValueMap);

        log.info("Spring's context was passed over to Isis");

        // dump config to log
        if(log.isInfoEnabled() && !isisSystemEnvironment.isUnitTesting()) {
            log.info("\n" + _Config.getConfiguration().toStringFormatted(isisSystemEnvironment));
        }    
        
        val isisConfigurationLegacy = _Config.getConfiguration(); // finalize config
        return isisConfigurationLegacy;
    }

}
