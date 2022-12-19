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
package org.apache.isis.runtime.memento;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.runtime.system.context.IsisContext;

/**
 * TODO[2112] possibly not final 
 * 
 * @since 2.0
 * 
 *
 */
public interface ObjectAdapterMementoSupport {

    ObjectAdapterMemento mementoForRootOid(RootOid rootOid);

    ObjectAdapterMemento mementoForAdapter(ObjectAdapter adapter);

    ObjectAdapterMemento mementoForPojo(Object pojo);

    ObjectAdapter reconstructObjectAdapter(ObjectAdapterMemento memento);

    // -- SPI

    static ObjectAdapterMementoSupport current() {
        return IsisContext.getServiceRegistry().lookupServiceElseFail(ObjectAdapterMementoSupport.class);
    }




}
