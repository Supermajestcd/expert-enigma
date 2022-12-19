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
package org.apache.isis.persistence.jdo.metamodel.facets.object.domainobject.objectspecid;

import java.util.Locale;
import java.util.Optional;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacet;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacetAbstract;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public class LogicalTypeFacetFromJdoPersistenceCapableFacet
extends LogicalTypeFacetAbstract {

    public static Optional<LogicalTypeFacet> create(
            final JdoPersistenceCapableFacet persistenceCapableFacet,
            final Class<?> correspondingClass,
            final FacetHolder holder) {

        if(persistenceCapableFacet.getPrecedence().isFallback()) {
            return Optional.empty();
        }
        final String schema = persistenceCapableFacet.getSchema();
        if(_Strings.isNullOrEmpty(schema)) {
            return Optional.empty();
        }
        final String logicalTypeName =
                schema.toLowerCase(Locale.ROOT) + "." + persistenceCapableFacet.getTable();
        return Optional.of(new LogicalTypeFacetFromJdoPersistenceCapableFacet(
                LogicalType.eager(correspondingClass, logicalTypeName),
                holder));
    }

    private LogicalTypeFacetFromJdoPersistenceCapableFacet(
            final LogicalType logicalType,
            final FacetHolder holder) {
        super(logicalType, holder, Precedence.INFERRED);
    }
}
