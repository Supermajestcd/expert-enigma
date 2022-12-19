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
package org.apache.isis.core.metamodel.methods;

import java.util.function.IntFunction;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

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
    
    // -- SUPPORTING METHOD NAMING CONVENTIONS

    protected static final Can<String> getNamingConventionForActionSupport(
            final ProcessMethodContext pmContext, 
            final String prefix) {
        val actionMethod = pmContext.getMethod();
        val isMixin = pmContext.isMixinMain();
        return MethodLiteralConstants.NAMING_ACTIONS
                .map(naming->naming.getActionSupportingMethodName(actionMethod, prefix, isMixin));
    }
    
    protected static final Can<IntFunction<String>> getNamingConventionForParameterSupport(
            final ProcessMethodContext pmContext, 
            final String prefix) {
        val actionMethod = pmContext.getMethod();
        val isMixin = pmContext.isMixinMain();
        return MethodLiteralConstants.NAMING_PARAMETERS
                .map(naming->naming.providerForParam(actionMethod, prefix, isMixin));
    }
    
    protected static final Can<String> getNamingConventionForPropertyAndCollectionSupport(
            final ProcessMethodContext pmContext, 
            final String prefix) {
        val actionMethod = pmContext.getMethod();
        val isMixin = pmContext.isMixinMain();
        return MethodLiteralConstants.NAMING_PROPERTIES_AND_COLLECTIONS
                .map(naming->naming.getMemberSupportingMethodName(actionMethod, prefix, isMixin));
    }
    
    // -- PROGRAMMING MODEL
    
    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        if(orphanValidation == OrphanValidation.DONT_VALIDATE
                || getConfiguration().getApplib().getAnnotation().getAction().isExplicit()) {
            return;
        }
        
        val noParamsOnly = getConfiguration().getCore().getMetaModel().getValidator().isNoParamsOnly();

        programmingModel.addValidator(new MetaModelVisitingValidatorAbstract() {

            @Override
            public String toString() {
                return "MetaModelValidatorVisiting.Visitor : MethodPrefixBasedFacetFactoryAbstract : " + prefixes.toList().toString();
            }

            @Override
            public void validate(ObjectSpecification spec) {
                
                if(spec.isManagedBean()) {
                    return;
                }

                // as an optimization only checking declared members (skipping inherited ones)  
                
                // ensure accepted actions do not have any of the reserved prefixes
                spec.streamDeclaredActions(MixedIn.EXCLUDED)
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

                            val messageFormat = "%s#%s: has prefix %s, is probably intended as a supporting method "
                                    + "for a property, collection or action%s.  If the method is intended to "
                                    + "be an action, then rename and use @ActionLayout(named=\"...\") or ignore "
                                    + "completely using @Programmatic";

                            ValidationFailure.raise(
                                    spec,
                                    String.format(
                                            messageFormat, 
                                            spec.getIdentifier().getClassName(),
                                            actionId,
                                            prefix,
                                            explanation));
                        }
                    }
                });

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
