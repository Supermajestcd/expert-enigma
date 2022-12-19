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
package org.apache.causeway.persistence.jdo.integration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.core.runtime.CausewayModuleCoreRuntime;
import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;
import org.apache.causeway.persistence.jdo.applib.CausewayModulePersistenceJdoApplib;
import org.apache.causeway.persistence.jdo.metamodel.CausewayModulePersistenceJdoMetamodel;

@Configuration
@Import({
        // modules
        CausewayModuleCoreRuntime.class,
        CausewayModulePersistenceCommons.class,
        CausewayModulePersistenceJdoApplib.class,
        CausewayModulePersistenceJdoMetamodel.class,

})
public class CausewayModulePersistenceJdoIntegration {

}
