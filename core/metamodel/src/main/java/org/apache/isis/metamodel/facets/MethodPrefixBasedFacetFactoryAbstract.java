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

package org.apache.isis.metamodel.facets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;

public abstract class MethodPrefixBasedFacetFactoryAbstract
extends FacetFactoryAbstract
implements MethodPrefixBasedFacetFactory {

    private final List<String> prefixes;

    private final OrphanValidation orphanValidation; 

    protected enum OrphanValidation { // remove?
        VALIDATE,
        DONT_VALIDATE
    }

    public MethodPrefixBasedFacetFactoryAbstract(final List<FeatureType> featureTypes, final OrphanValidation orphanValidation, final String... prefixes) {
        super(featureTypes);
        this.orphanValidation = orphanValidation;
        this.prefixes = Collections.unmodifiableList(Arrays.asList(prefixes));
    }
    
    @Override
    public List<String> getPrefixes() {
        return prefixes;
    }

 
    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        
//XXX[2161] replaced by SupportingMethodValidatorRefinerFactory, which changes behavior!     
        
//        if(orphanValidation == OrphanValidation.DONT_VALIDATE) {
//            return;
//        }
//
//        val noParamsOnly = _Config.getConfiguration().getBoolean(
//                ISIS_REFLECTOR_VALIDATOR_NO_PARAMS_ONLY_KEY,
//                ISIS_REFLECTOR_VALIDATOR_NO_PARAMS_ONLY_DEFAULT);
//
//        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {
//
//            @Override
//            public boolean visit(final ObjectSpecification objectSpec, final ValidationFailures validationFailures) {
//
//                
//                val objectActionStream = objectSpec.streamObjectActions(Contributed.EXCLUDED);
//                
//                objectActionStream.forEach(objectAction->{
//                    for (final String prefix : prefixes) {
//                        String actionId = objectAction.getId();
//
//                        if (actionId.startsWith(prefix) && prefix.length() < actionId.length()) {
//
//                            val explanation =
//                                    objectAction.getParameterCount() > 0 && 
//                                    noParamsOnly &&
//                                    (Objects.equals(prefix, MethodLiteralConstants.HIDE_PREFIX) || 
//                                            Objects.equals(prefix, MethodLiteralConstants.DISABLE_PREFIX))
//                                    ? " (note that such methods must have no parameters, '"
//                                        + ISIS_REFLECTOR_VALIDATOR_NO_PARAMS_ONLY_KEY
//                                        + "' config property)"
//                                            : "";
//
//                            val message = "%s#%s: has prefix %s, is probably intended as a supporting method for a property, collection or action%s.  If the method is intended to be an action, then rename and use @ActionLayout(named=\"...\") or ignore completely using @Programmatic";
//                            validationFailures.add(
//                                    objectSpec.getIdentifier(),
//                                    message,
//                                    objectSpec.getIdentifier().getClassName(),
//                                    actionId,
//                                    prefix,
//                                    explanation);
//                        }
//                    }
//                });
//
//                return true;
//            }
//        }));
    }

}
