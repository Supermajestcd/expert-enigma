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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoAppManifest.class,
})
public class DemoApp extends SpringBootServletInitializer {

    /**
     * 
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an 
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(String[] args) {
        //IsisPresets.prototyping();
        //IsisPresets.logging(IsisBeanScanInterceptorForSpring.class, "DEBUG");
        //IsisPresets.logging(IsisBeanTypeRegistry.class, "DEBUG");
        //IsisPresets.logging(org.apache.shiro.realm.AuthorizingRealm.class, "TRACE");
        //IsisPresets.logging(org.apache.isis.metamodel.authorization.standard.AuthorizationFacetAbstract.class, "DEBUG");
        //IsisPresets.logging(org.apache.isis.webapp.modules.templresources.TemplateResourceServlet.class, "DEBUG");
        SpringApplication.run(new Class[] { DemoApp.class }, args);
    }
}
