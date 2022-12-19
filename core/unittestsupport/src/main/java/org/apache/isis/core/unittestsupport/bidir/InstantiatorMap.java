/**
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
package org.apache.isis.core.unittestsupport.bidir;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

class InstantiatorMap  {

    private Map<Class<?>, Instantiator> instantiatorMap;

    public InstantiatorMap(ImmutableMap<Class<?>, Instantiator> instantiatorMap) {
        this.instantiatorMap = Maps.newHashMap(instantiatorMap);
    }

    Instantiator get(Class<?> cls) {
        return instantiatorMap.get(cls);
    }

    Instantiator put(Class<?> cls, Instantiator instantiator) {
        
        if(instantiator != null) {
            // check it works instantiator
            try {
                @SuppressWarnings("unused")
                final Object dummy = instantiator.instantiate();
            } catch(RuntimeException ex) {
                instantiator = Instantiator.NOOP;
            }
        } else {
            instantiator = Instantiator.NOOP;
        }
        
        instantiatorMap.put(cls, instantiator);
        
        return instantiator;
    }

}
