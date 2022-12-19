/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.swagger.internal;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.services.swagger.internal.ValuePropertyFactory.Factory;

public interface ValuePropertyPlugin {

    // -- CONTRACT

    public static interface ValuePropertyCollector {
        public void addValueProperty(final Class<?> cls, final ValuePropertyFactory.Factory factory);
        public void visitEntries(BiConsumer<Class<?>, ValuePropertyFactory.Factory> visitor);
    }

    public static ValuePropertyCollector collector() {

        return new ValuePropertyCollector() {

            final Map<Class<?>, Factory> entries = _Maps.newHashMap();

            @Override
            public void visitEntries(BiConsumer<Class<?>, Factory> visitor) {
                Objects.requireNonNull(visitor);
                entries.forEach(visitor);
            }

            @Override
            public void addValueProperty(Class<?> cls, Factory factory) {
                Objects.requireNonNull(cls);
                Objects.requireNonNull(factory);
                entries.put(cls, factory);
            }
        };

    }

    // -- INTERFACE

    public void plugin(ValuePropertyCollector collector);

}
