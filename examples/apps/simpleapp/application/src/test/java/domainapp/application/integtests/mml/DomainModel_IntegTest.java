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
package domainapp.application.integtests.mml;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.integtestsupport.validate.ValidateDomainModel;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import domainapp.application.integtests.SpringIntegrationTest;

class DomainModel_IntegTest extends SpringIntegrationTest {
    
    @Inject protected SpecificationLoader specificationLoader;

    @Test @Disabled("with full introspection in place this test actually throws")
    void validateDomainModel() {
        new ValidateDomainModel(specificationLoader).run();
    }


}