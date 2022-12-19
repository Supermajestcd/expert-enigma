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
package org.apache.isis.core.metamodel.postprocessors.all.i18n;


import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.described.ColumnDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.i8n.HasMemoizableTranslation;
import org.apache.isis.core.metamodel.facets.all.named.ColumnNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class TranslationPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public TranslationPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public boolean isEnabled() {
        // force PoWriter to be called to capture text that needs translating
        return super.getMetaModelContext().getTranslationService().getMode().isWrite();
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification) {
        memoizeTranslations(objectSpecification);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction act) {
        memoizeTranslations(act);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter param) {
        memoizeTranslations(param);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        memoizeTranslations(prop);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        memoizeTranslations(coll);

    }

    // -- HELPER

    private void memoizeTranslations(final FacetHolder facetHolder) {

        Stream.<Optional<? extends Facet>>of(
                facetHolder.lookupFacet(ObjectNamedFacet.class),
                facetHolder.lookupFacet(MemberNamedFacet.class),
                facetHolder.lookupFacet(ColumnNamedFacet.class),
                facetHolder.lookupFacet(ParamNamedFacet.class),
                facetHolder.lookupFacet(ObjectDescribedFacet.class),
                facetHolder.lookupFacet(MemberDescribedFacet.class),
                facetHolder.lookupFacet(ColumnDescribedFacet.class),
                facetHolder.lookupFacet(ParamDescribedFacet.class))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(facet->facet instanceof HasMemoizableTranslation)
        .map(HasMemoizableTranslation.class::cast)
        .forEach(HasMemoizableTranslation::memoizeTranslations);

    }


}
