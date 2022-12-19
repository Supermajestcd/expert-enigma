/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.metamodel.facets.object.domainobjectlayout;

import java.util.Optional;

import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaFacetAbstract;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaPosition;

public class CssClassFaFacetForViewModelLayoutAnnotation extends CssClassFaFacetAbstract {

    public static CssClassFaFacet create(
            final Optional<ViewModelLayout> viewModelLayoutIfAny, 
            final FacetHolder holder) {

        class Annot {
            private Annot(final ViewModelLayout viewModelLayout) {
                this.cssClassFa = _Strings.emptyToNull(viewModelLayout.cssClassFa());
                this.cssClassFaPosition = CssClassFaPosition.from(viewModelLayout.cssClassFaPosition());
            }
            String cssClassFa;
            CssClassFaPosition cssClassFaPosition;
        }

        return viewModelLayoutIfAny
                .map(Annot::new)
                .filter(a -> a.cssClassFa != null )
                .map(a -> new CssClassFaFacetForViewModelLayoutAnnotation(a.cssClassFa, a.cssClassFaPosition, holder))
                .orElse(null);
    }

    public CssClassFaFacetForViewModelLayoutAnnotation(final String value, CssClassFaPosition position,
            final FacetHolder holder) {
        super(value, position, holder);
    }
}
