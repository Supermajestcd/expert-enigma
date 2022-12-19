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
package org.apache.isis.persistence.jdo.datanucleus.schema;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.persistence.jdo.datanucleus.config.DnSettings;
import org.apache.isis.persistence.jdo.datanucleus.config.JdoEntityTypeRegistry;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class _DnApplication {

    private _DnApplicationComponents dnApplicationComponents;
    private final JdoEntityTypeRegistry jdoEntityTypeRegistry = new JdoEntityTypeRegistry();

    public _DnApplication(
            final MetaModelContext metaModelContext,
            final DnSettings dnSettings) {

        dnApplicationComponents = createDataNucleusApplicationComponents(
                metaModelContext,
                dnSettings);
    }

    public void shutdown() {
        dnApplicationComponents.shutdown();
    }

    // -- HELPER

    private _DnApplicationComponents createDataNucleusApplicationComponents(
            final MetaModelContext metaModelContext,
            final DnSettings dnSettings) {

        val configuration = metaModelContext.getConfiguration();
        val isisBeanTypeRegistry = metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(IsisBeanTypeRegistry.class);

        val classesToBePersisted = jdoEntityTypeRegistry.getEntityTypes(isisBeanTypeRegistry);

        if(log.isDebugEnabled()) {
            log.debug("Entity types discovered:");
            _NullSafe.stream(classesToBePersisted)
                .forEach(entityClassName->log.debug(" - {}", entityClassName));
        }

        val dataNucleusApplicationComponents = new _DnApplicationComponents(
                configuration,
                dnSettings.getAsProperties(),
                classesToBePersisted);

        _DnApplicationComponents.catalogNamedQueries(metaModelContext, classesToBePersisted);

        return dataNucleusApplicationComponents;
    }

}
