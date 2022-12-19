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
package domainapp.application;

import org.apache.isis.config.Presets;
import org.apache.isis.security.shiro.IsisBootSecurityShiro;
import org.apache.isis.viewer.wicket.viewer.IsisBootWebWicket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import domainapp.application.manifest.DomainAppAppManifest;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DomainAppAppManifest.class,
    IsisBootWebWicket.class,
    IsisBootSecurityShiro.class
})
@PropertySource("classpath:/domainapp/application/isis.properties")
public class SimpleApp extends SpringBootServletInitializer {

	/**
	 * 
	 * @param args
	 * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an 
	 * entry point by searching for classes having a {@code main(...)}
	 */
	public static void main(String[] args) {
	    Presets.prototyping();
		SpringApplication.run(new Class[] { SimpleApp.class }, args);
	}
	
}
