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

package org.apache.isis.viewer.wicket.model.models;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;

import lombok.val;
/**
 * Backing model for actions of application services menu bar (typically, as
 * displayed along the top or side of the page).
 */
public class ServiceActionsModel extends ModelAbstract<List<ManagedObject>> {

    private static final long serialVersionUID = 1L;

    private final DomainServiceLayout.MenuBar menuBar;

    /**
     * @param menuBar - may be null in special case of rendering the tertiary menu on the error page.
     */
    public ServiceActionsModel(IsisWebAppCommonContext commonContext, DomainServiceLayout.MenuBar menuBar) {
        super(commonContext);
        this.menuBar = menuBar;
    }

    /**
     * The menu bar being rendered; may be null in special case of rendering the tertiary menu on the error page.
     */
    public DomainServiceLayout.MenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    protected List<ManagedObject> load() {
        return super.getCommonContext().streamServiceAdapters()
                .filter(with(menuBar))
                .collect(Collectors.toList());
    }

    private static Predicate<ManagedObject> with(final DomainServiceLayout.MenuBar menuBar) {
        return (ManagedObject adapter) -> {
            val domainServiceLayoutFacet = adapter.getSpecification()
                    .getFacet(DomainServiceLayoutFacet.class);
            return (domainServiceLayoutFacet != null && domainServiceLayoutFacet.getMenuBar() == menuBar) ||
                    (domainServiceLayoutFacet == null && menuBar == DomainServiceLayout.MenuBar.PRIMARY);
        };
    }

}
