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
package org.apache.isis.applib.services.inject;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.springframework.beans.factory.InjectionPoint;

import lombok.val;

/**
 * Resolves injection points using the
 * {@link org.apache.isis.applib.services.registry.ServiceRegistry} (in other
 * words provides a domain service instance to all fields and setters that are
 * annotated with {@link javax.inject.Inject}).
 *
 * @since 1.x extended in 2.0 {@index}
 */
public interface ServiceInjector {

    /**
     * Injects domain services into the object, and calls the provided
     * {@link Consumer} for any non-resolvable injection points.
     *
     * @param domainObject
     * @param onNotResolvable
     * @param <T>
     * @return
     */
    <T> T injectServicesInto(final T domainObject, Consumer<InjectionPoint> onNotResolvable);

    /**
     * Injecs domain services into the object, and throws a
     * {@link NoSuchElementException} for any injection points that cannot be resolved.
     *
     * @param domainObject
     * @param <T>
     * @return
     */
    default <T> T injectServicesInto(final T domainObject) {

        return injectServicesInto(domainObject, injectionPoint->{

            val injectionPointName = injectionPoint.toString();
            val requiredType = injectionPoint.getDeclaredType();
            val msg = String
                    .format("Could not resolve injection point [%s] in target '%s' of required type '%s'",
                            injectionPointName,
                            domainObject.getClass().getName(),
                            requiredType);
            throw new NoSuchElementException(msg);
        });

        // ...
    }

}
