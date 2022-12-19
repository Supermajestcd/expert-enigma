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

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;

public interface ApplicationTenancy {

    public static final int MAX_LENGTH_PATH = 255;
    public static final int MAX_LENGTH_NAME = 40;
    public static final int TYPICAL_LENGTH_NAME = 20;
    
    // -- DOMAIN EVENTS
    
    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationTenancy, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationTenancy, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy> {}
    
    public static class AddUserDomainEvent extends ActionDomainEvent {}
    public static class RemoveUserDomainEvent extends ActionDomainEvent {}
    public static class AddChildDomainEvent extends ActionDomainEvent {}
    public static class DeleteDomainEvent extends ActionDomainEvent {}
    public static class RemoveChildDomainEvent extends ActionDomainEvent {}
    public static class UpdateNameDomainEvent extends ActionDomainEvent {}
    public static class UpdateParentDomainEvent extends ActionDomainEvent {}
    
    // -- MODEL
    
    public String getPath();

    public String getName();
    public void setName(String name);

    public ApplicationTenancy getParent();
    

}
