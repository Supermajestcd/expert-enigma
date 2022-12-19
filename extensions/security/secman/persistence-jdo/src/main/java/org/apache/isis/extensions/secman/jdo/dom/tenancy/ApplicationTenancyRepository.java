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
package org.apache.isis.extensions.secman.jdo.dom.tenancy;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;

import lombok.NonNull;
import lombok.val;

@Repository
@Named("isis.ext.secman.ApplicationTenancyRepository")
public class ApplicationTenancyRepository
implements org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyRepository {

    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;
    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;

    @Override
    public ApplicationTenancy newApplicationTenancy() {
        return factory.detachedEntity(new ApplicationTenancy());
    }


    // -- findByNameOrPathMatching

    @Override
    public Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> findByNameOrPathMatchingCached(final String search) {
        return queryResultsCacheProvider.get().execute(
                () -> findByNameOrPathMatching(search),
                ApplicationTenancyRepository.class,
                "findByNameOrPathMatchingCached", search);
    }

    private Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> findByNameOrPathMatching(final String search) {
        return _Casts.uncheckedCast(findByNameOrPathMatching_(search));
    }

    private SortedSet<ApplicationTenancy> findByNameOrPathMatching_(String search) {
        if (search == null) {
            return Collections.emptySortedSet();
        }
        return repository.allMatches(Query.named(ApplicationTenancy.class, "findByNameOrPathMatching")
                .withParameter("regex", String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."))))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }


    // -- findByName

    public ApplicationTenancy findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(
                () -> findByName(name),
                ApplicationTenancyRepository.class,
                "findByNameCached", name);
    }

    public ApplicationTenancy findByName(final String name) {
        return repository.uniqueMatch(
                    Query.named(ApplicationTenancy.class, "findByName")
                         .withParameter("name", name))
                .orElse(null);
    }


    // -- findByPath

    public ApplicationTenancy findByPathCached(final String path) {
        return queryResultsCacheProvider.get().execute(
                () -> findByPath(path),
                ApplicationTenancyRepository.class,
                "findByPathCached", path);
    }

    public ApplicationTenancy findByPath(final String path) {
        if (path == null) {
            return null;
        }
        return repository.uniqueMatch(
                    Query.named(ApplicationTenancy.class, "findByPath")
                         .withParameter("path", path))
                .orElse(null);
    }


    // -- autoComplete
    @Override
    public Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> findMatching(final String search) {
        return _Strings.isNullOrEmpty(search)
                ? Collections.emptySortedSet()
                : findByNameOrPathMatching(search);
    }

    // -- newTenancy

    @Override
    public ApplicationTenancy newTenancy(
            final String name,
            final String path,
            final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy parent) {
        ApplicationTenancy tenancy = findByPath(path);
        if (tenancy == null) {
            tenancy = newApplicationTenancy();
            tenancy.setName(name);
            tenancy.setPath(path);
            final ApplicationTenancy parentJdo = (ApplicationTenancy) parent;
            tenancy.setParent(parentJdo);
            if(parentJdo != null) {
                // although explicit maintenance of the children is normally not needed,
                // DN 5.x by default logs a warning if it discovers a mismatch; this quietens that
                parentJdo.getChildren().add(tenancy);
            }
            repository.persist(tenancy);
        }
        return tenancy;
    }

    // --

    @Override
    public Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> allTenancies() {
        return queryResultsCacheProvider.get().execute(
                this::allTenanciesNoCache,
                ApplicationTenancyRepository.class, "allTenancies");
    }

    public Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> allTenanciesNoCache() {
        return _Casts.uncheckedCast(allTenanciesNoCache_());
    }

    private SortedSet<ApplicationTenancy> allTenanciesNoCache_() {
        return repository.allInstances(ApplicationTenancy.class)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public void setTenancyOnUser(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy,
            @NonNull final org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        user.setAtPath(tenancy.getPath());
    }

    @Override
    public void clearTenancyOnUser(
            @NonNull final org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        user.setAtPath(null);
    }

    @Override
    public void setParentOnTenancy(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy,
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericParent) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        val parent = _Casts.<ApplicationTenancy>uncheckedCast(genericParent);
        // although explicit maintenance of the children is normally not needed,
        // DN 5.x by default logs a warning if it discovers a mismatch; this quietens that
        tenancy.setParent(parent);
        parent.getChildren().add(tenancy);
    }

    @Override
    public void clearParentOnTenancy(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        // although explicit maintenance of the children is normally not needed,
        // DN 5.x by default logs a warning if it discovers a mismatch; this quietens that
        final ApplicationTenancy parent = tenancy.getParent();
        if(parent != null) {
            parent.getChildren().add(tenancy);
            tenancy.setParent(null);
        }
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy> getChildren(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy) {
        return _Casts.uncheckedCast(getChildren_(genericTenancy));
    }

    private SortedSet<ApplicationTenancy> getChildren_(org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        return tenancy.getChildren()
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

}
