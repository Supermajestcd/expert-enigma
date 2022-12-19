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

package org.apache.isis.runtime.services.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.runtime.system.persistence.PersistenceSession;

import lombok.val;

@Service
public class RepositoryServiceDefault implements RepositoryService {

    @Inject private FactoryService factoryService;
    @Inject private WrapperFactory wrapperFactory;
    @Inject private TransactionService transactionService;
    @Inject private MetaModelContext metaModelContext;
    @Inject private IsisConfiguration isisConfiguration;
    
    private boolean autoFlush;

    @PostConstruct
    public void init() {
        val disableAutoFlush = isisConfiguration.getServices().getContainer().isDisableAutoFlush();
        this.autoFlush = !disableAutoFlush;
    }


    @Override
    public <T> T instantiate(final Class<T> domainClass) {
        return factoryService.instantiate(domainClass);
    }

    @Override
    public boolean isPersistent(final Object domainObject) {
        
        val adapter = metaModelContext.getObjectManager().adapt(unwrapped(domainObject));
        
        val spec = adapter.getSpecification();
        if(spec.isManagedBean() || spec.isViewModel()) {
            // services and view models are treated as persistent objects
            //FIXME bad design: this method should instead throw an IllegalArgEx. when called with non entity types!
            return true; 
        }
        
        val entityState = ManagedObject._entityState(adapter);
        val isRepresentingPersistent = entityState!=null 
                && (entityState.isAttached() || entityState.isDestroyed());
        return isRepresentingPersistent;
    }

    @Override
    public boolean isDeleted(final Object domainObject) {
        val adapter = metaModelContext.getObjectManager().adapt(unwrapped(domainObject));
        return ManagedObject._isDestroyed(adapter);
    }


    @Override
    public <T> T persist(final T object) {
        if (isPersistent(object)) {
            return object;
        }
        val adapter = getPersistenceSession().adapterFor(unwrapped(object));

        if(adapter == null) {
            throw new PersistFailedException("Object not known to framework (unable to create/obtain an adapter)");
        }
        if (adapter.isParentedCollection()) {
            // TODO check aggregation is supported
            return  object;
        }
        if (isPersistent(object)) {
            throw new PersistFailedException("Object already persistent; OID=" + adapter.getOid());
        }
        getPersistenceSession().makePersistentInTransaction(adapter);

        return object;
    }


    @Override
    public <T> T persistAndFlush(final T object) {
        persist(object);
        transactionService.flushTransaction();
        return object;
    }

    @Override
    public void remove(final Object domainObject) {
        removeIfNotAlready(domainObject);
    }

    private void removeIfNotAlready(final Object object) {
        if (!isPersistent(object)) {
            return;
        }
        if (object == null) {
            throw new IllegalArgumentException("Must specify a reference for disposing an object");
        }
        val adapter = getPersistenceSession().adapterFor(unwrapped(object));
        if (!isPersistent(object)) {
            throw new RepositoryException("Object not persistent: " + adapter);
        }

        getPersistenceSession().destroyObjectInTransaction(adapter);
    }

    @Override
    public void removeAndFlush(final Object domainObject) {
        remove(domainObject);
        transactionService.flushTransaction();
    }



    // -- allInstances, allMatches, uniqueMatch, firstMatch

    @Override
    public <T> List<T> allInstances(final Class<T> type) {
        return allMatches(new QueryFindAllInstances<T>(type, 0L, Long.MAX_VALUE));
    }

    @Override
    public <T> List<T> allInstances(final Class<T> type, long start, long count) {
        return allMatches(new QueryFindAllInstances<T>(type, start, count));
    }


    @Override
    public <T> List<T> allMatches(Class<T> ofType, Predicate<? super T> predicate) {
        return allMatches(ofType, predicate, 0L, Long.MAX_VALUE);
    }


    @Override
    public <T> List<T> allMatches(Class<T> ofType, final Predicate<? super T> predicate, long start, long count) {
        return _NullSafe.stream(allInstances(ofType, start, count))
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        if(autoFlush) {
            transactionService.flushTransaction();
        }
        return submitQuery(query);
    }

    <T> List<T> submitQuery(final Query<T> query) {
        val allMatching = getPersistenceSession().allMatchingQuery(query);
        return _Casts.uncheckedCast(ManagedObject.unwrapPojoListElseEmpty(allMatching));
    }



    @Override
    public <T> Optional<T> uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + predicate);
        }
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseEmpty(instances);
    }

    @Override
    public <T> Optional<T> firstMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> firstMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        return firstInstanceElseEmpty(instances);
    }

    private static <T> Optional<T> firstInstanceElseEmpty(final List<T> instances) {
        return instances.size() == 0
                ? Optional.empty()
                : Optional.of(instances.get(0));
    }


    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }
    
    protected PersistenceSession getPersistenceSession() {
        return PersistenceSession.current(PersistenceSession.class)
                .getFirst()
                .orElseThrow(()->new NonRecoverableException("No IsisSession on current thread."));
    }


}
