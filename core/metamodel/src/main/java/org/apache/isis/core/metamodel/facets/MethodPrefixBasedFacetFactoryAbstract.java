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
package org.apache.isis.core.metamodel.facets;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class MethodPrefixBasedFacetFactoryAbstract
extends FacetFactoryAbstract
implements MethodPrefixBasedFacetFactory {

    @Getter(onMethod = @__(@Override))
    private final Can<String> prefixes;

    private final OrphanValidation orphanValidation;

    protected enum OrphanValidation {
        VALIDATE,
        DONT_VALIDATE
    }

    public MethodPrefixBasedFacetFactoryAbstract(
            @NonNull final ImmutableEnumSet<FeatureType> featureTypes, 
            @NonNull final OrphanValidation orphanValidation, 
            @NonNull final Can<String> prefixes) {
        
        super(featureTypes);
        this.orphanValidation = orphanValidation;
        this.prefixes = prefixes;
    }
    
    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        if(orphanValidation == OrphanValidation.DONT_VALIDATE
                || getConfiguration().getApplib().getAnnotation().getAction().isExplicit()) {
            return;
        }
        
        val noParamsOnly = getConfiguration().getCore().getMetaModel().getValidator().isNoParamsOnly();

        programmingModel.addValidator(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public String toString() {
                return "MetaModelValidatorVisiting.Visitor : MethodPrefixBasedFacetFactoryAbstract : " + prefixes.toList().toString();
            }

            @Override
            public boolean visit(ObjectSpecification objectSpec, MetaModelValidator metaModelValidator) {

                // ensure accepted actions do not have any of the reserved prefixes
                objectSpec.streamObjectActions(Contributed.EXCLUDED)
                        .forEach(objectAction -> {

                            val actionId = objectAction.getId();

                            for (val prefix : prefixes) {

                                if (isPrefixed(actionId, prefix)) {

                                    val explanation =
                                            objectAction.getParameterCount() > 0
                                                    && noParamsOnly
                                                    && (MethodLiteralConstants.HIDE_PREFIX.equals(prefix)
                                                    || MethodLiteralConstants.DISABLE_PREFIX.equals(prefix))
                                                    ? " (such methods must have no parameters, '"
                                                    + "isis.core.meta-model.validator.no-params-only"
                                                    + "' config property)"
                                                    : "";

                                    val message = "%s#%s: has prefix %s, is probably intended as a supporting method "
                                            + "for a property, collection or action%s.  If the method is intended to "
                                            + "be an action, then rename and use @ActionLayout(named=\"...\") or ignore "
                                            + "completely using @Programmatic";

                                    metaModelValidator.onFailure(
                                            objectSpec,
                                            objectSpec.getIdentifier(),
                                            message,
                                            objectSpec.getIdentifier().getClassName(),
                                            actionId,
                                            prefix,
                                            explanation);
                                }
                            }
                        });

                return true;

            }
        });
    }

    protected boolean isPropertyOrMixinMain(ProcessMethodContext processMethodContext) {
        return processMethodContext.isMixinMain() 
                || (
                        processMethodContext.getFeatureType()!=null // null check, yet to support some JUnit tests
                        && processMethodContext.getFeatureType().isProperty()
                   );
    }
    
    // -- HELPER
    
    private static boolean isPrefixed(String actionId, String prefix) {
        return actionId.startsWith(prefix) && actionId.length() > prefix.length();
    }



}
