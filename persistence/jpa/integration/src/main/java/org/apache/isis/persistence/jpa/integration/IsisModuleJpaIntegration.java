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
package org.apache.isis.persistence.jpa.integration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.persistence.jpa.integration.metamodel.JpaProgrammingModel;
import org.apache.isis.persistence.jpa.integration.services.JpaSupportServiceUsingSpring;
import org.apache.isis.persistence.jpa.integration.typeconverters.JavaAwtBufferedImageByteArrayConverter;

@Configuration
@Import({
        // modules
        IsisModuleCoreRuntime.class,
//        IsisModulePersistenceJpaApplib.class,

        // @Component's
        JpaProgrammingModel.class,

        // @Service's
        JpaSupportServiceUsingSpring.class,

//        DataNucleusSettings.class,
//        ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException.class,
//        ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException.class,
//        ExceptionRecognizerForJDOObjectNotFoundException.class,
//        ExceptionRecognizerForJDODataStoreException.class,
//
//        IsisJdoSupportDN5.class,
//        IsisPlatformTransactionManagerForJdo.class,
//        JdoPersistenceLifecycleService.class,
//        PersistenceSessionFactory5.class,
//        JdoMetamodelMenu.class,
//
//        // @Mixin's
//        Persistable_datanucleusVersionLong.class,
//        Persistable_datanucleusVersionTimestamp.class,
//        Persistable_downloadJdoMetadata.class,
})
@EntityScan(basePackageClasses = {

        // @Converter's
        JavaAwtBufferedImageByteArrayConverter.class
})
public class IsisModuleJpaIntegration {

}
