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

package org.apache.isis.commons.internal.reflection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.jaxb.JaxbServiceDefault;
import org.apache.isis.core.metamodel.services.user.UserServiceDefault;

//TODO we are using real word classes from the framework, we could instead isolate these tests
// if we provide some custom classes for hierarchy traversal here (could be nested); 
// then move this test to the 'commons' module, where it belongs
class ReflectTest {
    
    @Test
    void typeHierarchy() {
        
        Class<?> type = UserServiceDefault.SudoServiceSpi.class;
        
        String typeListLiteral = _Reflect.streamTypeHierarchy(type, false)
        .map(t->t.getName())
        .collect(Collectors.joining(", "));
        
        Assertions.assertEquals(""
                + "org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi, "
                + "java.lang.Object", 
                typeListLiteral);
        
    }

    @Test
    void typeHierarchyAndInterfaces() {
        
        Class<?> type = UserServiceDefault.SudoServiceSpi.class;
        
        String typeListLiteral = _Reflect.streamTypeHierarchy(type, true)
        .map(t->t.getName())
        .collect(Collectors.joining(", "));
        
        Assertions.assertEquals(
                "org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi, "
                + "org.apache.isis.applib.services.sudo.SudoService$Spi, "
                + "java.lang.Object", 
                typeListLiteral);
        
    }
    
    @Test
    void allMethods() {
        
        Class<?> type = UserServiceDefault.SudoServiceSpi.class;
        
        String typeListLiteral = _Reflect.streamAllMethods(type, true)
        .map(m->m.toString())
        .collect(Collectors.joining(", "));
        
        Assertions.assertEquals(
                "public void org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi.runAs(java.lang.String,java.util.List), "
                + "public void org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi.releaseRunAs(), "
                + "public abstract void org.apache.isis.applib.services.sudo.SudoService$Spi.runAs(java.lang.String,java.util.List), "
                + "public abstract void org.apache.isis.applib.services.sudo.SudoService$Spi.releaseRunAs()", 
                typeListLiteral);
        
    }
    
    @Test
    void annotationLookup() throws NoSuchMethodException, SecurityException {
        
        Class<?> type = UserServiceDefault.SudoServiceSpi.class;
        Method method = type.getMethod("runAs", new Class[] {String.class, List.class});
        
        Programmatic annot = _Reflect.getAnnotation(method, Programmatic.class, true, true);
        
        Assertions.assertNotNull(annot);
    }
    
    @Test
    void typeHierarchyAndInterfaces2() {
        
        Class<?> type = JaxbServiceDefault.class;
        
        String typeListLiteral = _Reflect.streamTypeHierarchy(type, true)
        .map(t->t.getName())
        .collect(Collectors.joining(", "));
        
        Assertions.assertEquals(
                "org.apache.isis.applib.services.jaxb.JaxbServiceDefault, "
                + "org.apache.isis.applib.services.jaxb.JaxbService$Simple, "
                + "org.apache.isis.applib.services.jaxb.JaxbService, "
                + "java.lang.Object", 
                typeListLiteral);
        
    }
    
    
    @Test
    void annotationLookup2() throws NoSuchMethodException, SecurityException {
        
        Class<?> type = JaxbServiceDefault.class;
        Method method = type.getMethod("fromXml", new Class[] {JAXBContext.class, String.class, Map.class});
        
        Programmatic annot = _Reflect.getAnnotation(method, Programmatic.class, true, true);
        
        Assertions.assertNotNull(annot);
        
    }
    
}
