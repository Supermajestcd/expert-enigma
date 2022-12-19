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
package org.apache.isis.core.metamodel.facets.object.domainservice;


import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;


/**
 * Corresponds to annotating the class with the {@link org.apache.isis.applib.annotation.DomainService} annotation.
 */
public interface DomainServiceFacet extends Facet {

    /**
     * Corresponds to {@link org.apache.isis.applib.annotation.DomainService#nature()}.
     *
     * <p>
     *     If set to {@link org.apache.isis.applib.annotation.NatureOfService#DOMAIN}, 
     *     then {@link NotContributedFacet} and
     *     {@link NotInServiceMenuFacet} is
     *     derived for all actions.
     * </p>
     */
    public NatureOfService getNatureOfService();

}