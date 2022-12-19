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
package org.apache.isis.core.config.beans;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Vetoed;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.ioc.BeanSort;
import org.apache.isis.core.commons.internal.reflection._Reflect;

import static org.apache.isis.core.commons.internal.base._With.requires;
import static org.apache.isis.core.commons.internal.reflection._Annotations.findNearestAnnotation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Holds the set of domain services, persistent entities and fixture scripts etc.
 * @since 2.0
 */
@NoArgsConstructor @Log4j2
public final class IsisBeanTypeRegistry implements IsisComponentScanInterceptor, AutoCloseable {

    /**
     * Inbox for introspection, as used by the SpecificationLoader
     */
    private final Map<Class<?>, BeanSort> introspectableTypes = new HashMap<>();

    // -- DISTINCT CATEGORIES OF BEAN SORTS
    
    private final Map<Class<?>, String> managedBeanNamesByType = new HashMap<>();
    @Getter private final Set<Class<?>> entityTypes = new HashSet<>();
    @Getter private final Set<Class<?>> mixinTypes = new HashSet<>();
    @Getter private final Set<Class<?>> viewModelTypes = new HashSet<>();
    @Getter private final Set<Class<?>> vetoedTypes = _Sets.newConcurrentHashSet();
    
    private final List<Set<? extends Class<? extends Object>>> allCategorySets = _Lists.of(
            entityTypes,
            mixinTypes,
            viewModelTypes,
            vetoedTypes
            );

    @Override
    public void close() {

        managedBeanNamesByType.clear();
        introspectableTypes.clear();
        allCategorySets.forEach(Set::clear);
    }

    public Map<Class<?>, BeanSort> snapshotIntrospectableTypes() {

        final Map<Class<?>, BeanSort> defensiveCopy;

        synchronized (introspectableTypes) {
            defensiveCopy = new HashMap<>(introspectableTypes);
        }

        if(log.isDebugEnabled()) {
            defensiveCopy.forEach((k, v)->{
                log.debug("to be introspected: {} [{}]", k.getName(), v.name());
            });
        }

        return defensiveCopy;
    }
    
    public void veto(Class<?> type) {
        vetoedTypes.add(type);
    }

    // -- FILTER
    
    @Override
    public void intercept(TypeMetaData typeMeta) {
        
        val type = typeMeta.getUnderlyingClass();

        if(findNearestAnnotation(type, DomainObject.class).isPresent() ||
                findNearestAnnotation(type, ViewModel.class).isPresent() ||
                findNearestAnnotation(type, Mixin.class).isPresent() ||
                findNearestAnnotation(type, Vetoed.class).isPresent()) {
            
            typeMeta.setInjectable(false); // reject
            
        } else {
            typeMeta.setBeanNameOverride(extractObjectType(type).orElse(null));
        }
        
        val beanSort = quickClassify(type);
        
        if(beanSort.isToBeIntrospected()) {
            addIntrospectableType(beanSort, typeMeta);
            
            if(log.isDebugEnabled()) {
                log.debug("to-be-introspected: {} [{}]",                        
                                type,
                                beanSort.name());
            }
        }
        
    }
    
    
    /**
     * If given type is part of the meta-model and is available for injection, 
     * returns the <em>Managed Bean's</em> name (id) as
     * recognized by the IoC container.
     *
     * @param type
     * @return
     */
    public Optional<String> getManagedBeanNameForType(Class<?> type) {
        if(vetoedTypes.contains(type)) { // vetos are coming from the spec-loader during init
            return Optional.empty();
        }
        return Optional.ofNullable(managedBeanNamesByType.get(type));
    }
    
    /**
     * Whether given type is part of the meta-model and is available for injection 
     * (is a <em>Managed Bean</em>). 
     * @param type
     */
    public boolean isManagedBean(Class<?> type) {
        return getManagedBeanNameForType(type).isPresent();
    }
    
    // -- HELPER

    private void addIntrospectableType(BeanSort sort, TypeMetaData typeMeta) {
        val type = typeMeta.getUnderlyingClass();
        synchronized (introspectableTypes) {
            introspectableTypes.put(type, sort);
            
            switch (sort) {
            case MANAGED_BEAN_CONTRIBUTING:
            case MANAGED_BEAN_NOT_CONTRIBUTING:
                managedBeanNamesByType.put(type, typeMeta.getEffectiveBeanName());
                return;
            case MIXIN:
                mixinTypes.add(type);
                return;
            case ENTITY:
                entityTypes.add(type);
                return;
            case VIEW_MODEL:
                viewModelTypes.add(type);
                return;
            
            // skip introspection for these
            case COLLECTION:
            case VALUE:
            case UNKNOWN:
                return;
            }
            
        }
    }
    
    public BeanSort quickClassify(Class<?> type) {

        requires(type, "type");
        
        if(findNearestAnnotation(type, Vetoed.class).isPresent()) {
            return BeanSort.UNKNOWN; // reject
        }

        val aDomainService = findNearestAnnotation(type, DomainService.class);
        if(aDomainService.isPresent()) {
            return BeanSort.MANAGED_BEAN_CONTRIBUTING;
        }

        //this takes precedence over whatever @DomainObject has to say
        if(_Reflect.containsAnnotation(type, "javax.jdo.annotations.PersistenceCapable")) {
            return BeanSort.ENTITY;
        }

        if(findNearestAnnotation(type, Mixin.class).isPresent()) {
            return BeanSort.MIXIN;
        }

        if(findNearestAnnotation(type, ViewModel.class).isPresent()) {
            return BeanSort.VIEW_MODEL;
        }

        if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
            return BeanSort.VIEW_MODEL;
        }

        val aDomainObject = findNearestAnnotation(type, DomainObject.class).orElse(null);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case MIXIN:
                return BeanSort.MIXIN;
            case JDO_ENTITY:
                return BeanSort.ENTITY;
            case EXTERNAL_ENTITY:
            case INMEMORY_ENTITY:
            case VIEW_MODEL:
            case NOT_SPECIFIED:
                return BeanSort.VIEW_MODEL; //because object is not associated with a persistence context unless discovered above
            } 
        }

        if(findNearestAnnotation(type, Component.class).isPresent()) {
            return BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING;
        }
        
        if(Collection.class.isAssignableFrom(type)) {
            return BeanSort.COLLECTION;
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return BeanSort.VALUE;
        }

        return BeanSort.UNKNOWN;
    }

    private Optional<String> extractObjectType(Class<?> type) {

        val aDomainService = _Reflect.getAnnotation(type, DomainService.class);
        if(aDomainService!=null) {
            val objectType = aDomainService.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return Optional.of(objectType); 
            }
            return Optional.empty(); // stop processing
        }

        return Optional.empty();

    }




}