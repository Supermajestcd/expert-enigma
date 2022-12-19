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
package org.apache.isis.extensions.secman.api.tenancy;

import java.util.Collection;

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationTenancy extends Comparable<ApplicationTenancy> {

    int MAX_LENGTH_PATH = 255;
    int MAX_LENGTH_NAME = 120;
    int TYPICAL_LENGTH_NAME = 20;


    // -- DOMAIN EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationTenancy, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationTenancy, T> {}
    abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}

    class AddUserDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    class RemoveUserDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    class AddChildDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    class DeleteDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    class RemoveChildDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    class UpdateNameDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    class UpdateParentDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}

    // -- MODEL

    default String title() {
        return getName();
    }

    String getPath();
    void setPath(String path);

    String getName();
    void setName(String name);

    ApplicationTenancy getParent();
    void setParent(ApplicationTenancy parent);

    Collection<ApplicationTenancy> getChildren();

}
