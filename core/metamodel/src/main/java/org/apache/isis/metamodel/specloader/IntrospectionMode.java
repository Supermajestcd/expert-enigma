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

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.plugins.environment.DeploymentType;
import org.apache.isis.metamodel.MetaModelContext;

import lombok.val;

public enum IntrospectionMode {

    /**
     * Lazy (don't introspect members for most classes unless required), 
     * irrespective of the deployment mode.
     */
    LAZY {
        @Override
        protected boolean isFullIntrospect(final DeploymentType deploymentType) {
            return false;
        }
    },

    /**
     * If production deployment mode, then full, otherwise lazy.
     */
    LAZY_UNLESS_PRODUCTION {
        @Override
        protected boolean isFullIntrospect(final DeploymentType deploymentType) {
            return deploymentType.isProduction();
        }
    },

    /**
     * Full introspection, irrespective of deployment mode.
     */
    FULL {
        @Override
        protected boolean isFullIntrospect(final DeploymentType deploymentType) {
            return true;
        }
    };

    /**
     * @return whether current introspection mode is 'full', dependent on current
     * deployment mode and configuration
     */
    public static boolean isFullIntrospect() {

        val config = MetaModelContext.current().getConfiguration();
        val introspectionMode = SpecificationLoader.CONFIG_PROPERTY_MODE.from(config);
        val deploymentMode = _Context.getEnvironment().getDeploymentType();

        return introspectionMode.isFullIntrospect(deploymentMode);
    }

    protected abstract boolean isFullIntrospect(final DeploymentType deploymentType);

}
