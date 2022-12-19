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
package demoapp.dom.types.javaawt.images.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Image;

@Service
public class JavaAwtImageJdoEntities {

    public Optional<JavaAwtImageJdo> find(final java.awt.Image readOnlyProperty) {
        return repositoryService.firstMatch(JavaAwtImageJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaAwtImageJdo> all() {
        return repositoryService.allInstances(JavaAwtImageJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}