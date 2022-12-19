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

package org.apache.isis.core.metamodel.facets.actions.debug.annotation;

import org.apache.isis.applib.annotation.Debug;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

/**
 * {@link DebugFacet} can also be installed via a naming convention, see
 * {@link org.apache.isis.core.metamodel.facets.actions.interaction.ActionNamedDebugExplorationFacetFactory}.
 *
 * @deprecated
 */
@Deprecated
public class DebugFacetAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner, IsisConfigurationAware {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(Debug.class);

    public DebugFacetAnnotationFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Debug annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Debug.class);
        final DebugFacet facet = create(annotation, processMethodContext.getFacetHolder());
        FacetUtil.addFacet(validator.flagIfPresent(facet, processMethodContext));
    }

    private DebugFacet create(final Debug annotation, final FacetHolder holder) {
        return annotation == null ? null : new DebugFacetAnnotation(holder);
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        validator.setConfiguration(configuration);
    }

}