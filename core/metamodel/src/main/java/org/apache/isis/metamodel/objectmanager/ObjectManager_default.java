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
package org.apache.isis.metamodel.objectmanager;

import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.metamodel.objectmanager.identify.ObjectIdentifier;
import org.apache.isis.metamodel.objectmanager.load.ObjectLoader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ObjectManager_default implements ObjectManager {

    @Getter(onMethod = @__(@Override)) private final MetaModelContext metaModelContext;
    @Getter(onMethod = @__(@Override)) private final ObjectLoader objectLoader;
    @Getter(onMethod = @__(@Override)) private final ObjectCreator objectCreator;
    @Getter(onMethod = @__(@Override)) private final ObjectIdentifier objectIdentifier;
    
}
