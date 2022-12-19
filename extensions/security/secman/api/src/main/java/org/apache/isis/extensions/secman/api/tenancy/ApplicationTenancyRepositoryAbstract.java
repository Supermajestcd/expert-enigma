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
import java.util.Collections;

import javax.inject.Inject;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ApplicationTenancyRepositoryAbstract<T extends ApplicationTenancy>
implements org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyRepository {

    private final Class<T> applicationTenancyClass;

    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;
    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;

    @Override
    public ApplicationTenancy newApplicationTenancy() {
        return factory.detachedEntity(applicationTenancyClass);
    }


    // -- findByNameOrPathMatching

    @Override
    public Collection<ApplicationTenancy> findByNameOrPathMatchingCached(final String search) {
        return queryResultsCacheProvider.get().execute(
                () -> findByNameOrPathMatching(search),
                ApplicationTenancyRepositoryAbstract.class,
                "findByNameOrPathMatchingCached", search);
    }

    private Collection<ApplicationTenancy> findByNameOrPathMatching(final String search) {
        if (_Strings.isNullOrEmpty(search)) {
            return Collections.emptySortedSet();
        }
        return repository.allMatches(
                Query.named(applicationTenancyClass, "findByNameOrPathMatching")
                    .withParameter("regex", regexified(search)))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    private static String regexified(String search) {
        return String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
    }


    // -- findByName

    public ApplicationTenancy findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(
                () -> findByName(name),
                ApplicationTenancyRepositoryAbstract.class,
                "findByNameCached", name);
    }

    public ApplicationTenancy findByName(final String name) {
        return repository.uniqueMatch(
                    Query.named(applicationTenancyClass, "findByName")
                         .withParameter("name", name))
                .orElse(null);
    }


    // -- findByPath

    public ApplicationTenancy findByPathCached(final String path) {
        return queryResultsCacheProvider.get().execute(
                () -> findByPath(path),
                ApplicationTenancyRepositoryAbstract.class,
                "findByPathCached", path);
    }

    public ApplicationTenancy findByPath(final String path) {
        if (path == null) {
            return null;
        }
        return repository.uniqueMatch(
                    Query.named(applicationTenancyClass, "findByPath")
                         .withParameter("path", path))
                .orElse(null);
    }


    // -- autoComplete
    @Override
    public Collection<ApplicationTenancy> findMatching(final String search) {
        if (_Strings.isNullOrEmpty(search)) {
            return Collections.emptySortedSet();
        }
        return findByNameOrPathMatching(search);
    }

    // -- newTenancy

    @Override
    public ApplicationTenancy newTenancy(
            final String name,
            final String path,
            final ApplicationTenancy parent) {
        ApplicationTenancy tenancy = findByPath(path);
        if (tenancy == null) {
            tenancy = newApplicationTenancy();
            tenancy.setName(name);
            tenancy.setPath(path);
            tenancy.setParent(parent);
            if(parent != null) {
                // although explicit maintenance of the children is normally not needed,
                // DN 5.x by default logs a warning if it discovers a mismatch; this quietens that
                parent.getChildren().add(tenancy);
            }
            repository.persist(tenancy);
        }
        return tenancy;
    }

    // --

    @Override
    public Collection<ApplicationTenancy> allTenancies() {
        return queryResultsCacheProvider.get().execute(
                this::allTenanciesNoCache,
                ApplicationTenancyRepositoryAbstract.class, "allTenancies");
    }

    public Collection<ApplicationTenancy> allTenanciesNoCache() {
        return repository.allInstances(ApplicationTenancy.class)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public void setTenancyOnUser(
            @NonNull final ApplicationTenancy tenancy,
            @NonNull final ApplicationUser user) {
        user.setAtPath(tenancy.getPath());
    }

    @Override
    public void clearTenancyOnUser(
            @NonNull final ApplicationUser user) {
        user.setAtPath(null);
    }

    @Override
    public void setParentOnTenancy(
            @NonNull final ApplicationTenancy tenancy,
            @NonNull final ApplicationTenancy parent) {
        // although explicit maintenance of the children is normally not needed,
        // DN 5.x by default logs a warning if it discovers a mismatch; this quietens that
        tenancy.setParent(parent);
        parent.getChildren().add(tenancy);
    }

    @Override
    public void clearParentOnTenancy(
            @NonNull final ApplicationTenancy tenancy) {
        // although explicit maintenance of the children is normally not needed,
        // DN 5.x by default logs a warning if it discovers a mismatch; this quietens that
        final ApplicationTenancy parent = tenancy.getParent();
        if(parent != null) {
            parent.getChildren().add(tenancy);
            tenancy.setParent(null);
        }
    }

    @Override
    public Collection<ApplicationTenancy> getChildren(
            @NonNull final ApplicationTenancy tenancy) {
        return tenancy.getChildren()
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

}
