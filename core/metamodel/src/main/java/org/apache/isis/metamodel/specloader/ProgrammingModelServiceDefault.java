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
package org.apache.isis.metamodel.specloader;

import javax.inject.Inject;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.progmodel.ProgrammingModelAbstract;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class ProgrammingModelServiceDefault implements ProgrammingModelService {

    @Override
    public ProgrammingModel get() {
        return programmingModel.get();
    }

    // -- HELPER

    @Inject private IsisConfigurationLegacy configurationLegacy;
    @Inject private IsisConfiguration configuration;

    private _Lazy<ProgrammingModel> programmingModel = 
            _Lazy.threadSafe(this::createProgrammingModel);

    private ProgrammingModel createProgrammingModel() {
        
        log.debug("About to create the ProgrammingModel.");

        boolean ignoreDep = configuration.getReflector().getFacets().isIgnoreDeprecated();
        val deprecatedPolicy = ignoreDep ? ProgrammingModelAbstract.DeprecatedPolicy.IGNORE : ProgrammingModelAbstract.DeprecatedPolicy.HONOUR;

        val programmingModel = new ProgrammingModelFacetsJava8(deprecatedPolicy);
        ProgrammingModel.Util.includeFacetFactories(configurationLegacy, programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(configurationLegacy, programmingModel);
        
        if(log.isDebugEnabled()) {
            
            val facetFactoryCount = programmingModel.getList().size();
            val postProcessorCount = programmingModel.getPostProcessors().size();
            
            log.debug("ProgrammingModel created with {} factories and {} post-processors.", 
                    facetFactoryCount, postProcessorCount);    
        }
        
        return programmingModel;
    }

}
