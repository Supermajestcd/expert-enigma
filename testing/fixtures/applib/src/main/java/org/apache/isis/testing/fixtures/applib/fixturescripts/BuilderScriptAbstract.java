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
package org.apache.isis.testing.fixtures.applib.fixturescripts;

import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.testing.fixtures.applib.api.FixtureScriptWithExecutionStrategy;
import org.apache.isis.testing.fixtures.applib.api.PersonaWithBuilderScript;
import org.apache.isis.testing.fixtures.applib.api.PersonaWithFinder;
import org.apache.isis.testing.fixtures.applib.api.WithPrereqs;

import lombok.Getter;

public abstract class BuilderScriptAbstract<T>
extends FixtureScript implements WithPrereqs<T>, FixtureScriptWithExecutionStrategy {

    @Getter(onMethod=@__({@Override}))
    private final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy;

    // -- FACTORIES
    
    /**
     * Typically we expect builders to have value semantics, so this is provided as a convenience.
     */
    protected BuilderScriptAbstract() {
        this(FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_VALUE);
    }

    protected BuilderScriptAbstract(final FixtureScripts.MultipleExecutionStrategy executionStrategy) {
        this.multipleExecutionStrategy = executionStrategy;
    }

    // -- RESULTING OBJECT
    
    public abstract T getObject();
    
    // -- RUN ANOTHER SCRIPT
    
    @Programmatic
    public BuilderScriptAbstract<T> build(
            final FixtureScript parentFixtureScript,
            final ExecutionContext executionContext) {

        parentFixtureScript.serviceInjector.injectServicesInto(this);

        execPrereqs(executionContext);

        // returns the fixture script that is run
        // (either this one, or possibly one previously executed).
        return executionContext.executeChildT(parentFixtureScript, this);
    }

    // -- RUN PERSONAS
    
    public T objectFor(
            final PersonaWithBuilderScript<BuilderScriptAbstract<T>> persona,
            final ExecutionContext executionContext) {

        if(persona == null) {
            return null;
        }
        final BuilderScriptAbstract<T> fixtureScript = persona.builder();
        return executionContext.executeChildT(this, fixtureScript).getObject();
    }

    // -- RUN FINDERS
    
    public T findUsing(final PersonaWithFinder<T> persona) {
        if(persona == null) {
            return null;
        }
        return persona.findUsing(serviceRegistry);
    }
    
    // -- PREREQUISITES

    private final List<WithPrereqs.Block<T>> prereqs = _Lists.newArrayList();

    @Override
    public BuilderScriptAbstract<T> setPrereq(WithPrereqs.Block<T> prereq) {
        prereqs.add(prereq);
        return this;
    }
    
    @Override
    public void execPrereqs(final ExecutionContext executionContext) {
        for (final WithPrereqs.Block<T> prereq : prereqs) {
            prereq.execute(this, executionContext);
        }
    }


}

