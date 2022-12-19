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
package org.apache.isis.testing.fixtures.applib.personas;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

/**
 * Provides a mechanism for {@link FixtureScript}s to specify prerequisites
 * to be executed first.
 *
 * <p>
 *     Most commonly used to chain {@link BuilderScriptAbstract}s in conjunction
 *     with personas.
 * </p>
 *
 * @since 2.x {@index}
 */
public interface WithPrereqs<T> {

    BuilderScriptAbstract<T> addPrereq(Block<T> prereq);

    void execPrereqs(FixtureScript.ExecutionContext executionContext);

    interface Block<T> {
        void execute(BuilderScriptAbstract<T> onFixture, FixtureScript.ExecutionContext executionContext);
    }

}

