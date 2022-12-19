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
package org.apache.isis.testdomain.model.good;

import java.util.List;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

import lombok.Getter;
import lombok.Setter;

/**
 * @see <a href="https://issues.apache.org/jira/browse/ISIS-2499">ISIS-2499</a>
 */
@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperElementTypeVm {

    @Collection
    @Getter @Setter private List<ElementTypeInterface> interfaceColl;

    @Collection
    @Getter @Setter private List<ElementTypeAbstract> abstractColl;

    @Collection
    @Getter @Setter private List<ElementTypeConcrete> concreteColl;

    @Collection
    @Getter @Setter private List<? extends ElementTypeInterface> interfaceColl2;

    @Collection
    @Getter @Setter private List<? extends ElementTypeAbstract> abstractColl2;

    @Collection
    @Getter @Setter private List<? extends ElementTypeConcrete> concreteColl2;

}
