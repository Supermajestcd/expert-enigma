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

package org.apache.isis.core.commons.internal.context;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.plugins.PluginResolveException;
import org.apache.isis.core.commons.internal.base._With;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilizes the Java 7+ service-provider loading facility.<br/>
 * see {@link java.util.ServiceLoader}
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public final class _Plugin {

    private _Plugin(){}

    /**
     * Returns all services implementing the interface or abstract class representing the {@code service}.
     * <p>
     * If com.example.impl.StandardCodecs is an implementation of the CodecSet service then its jar file also contains a file named
     * <pre>
     *      META-INF/services/com.example.CodecSet
     * </pre>
     * This file contains the single line:
     * <pre>
     *       com.example.impl.StandardCodecs  # Standard codecs
     * </pre>
     * </p>
     * @param service
     * @return non null
     */
    public static <S> Set<S> loadAll(Class<S> service){
        _With.requires(service, "service");

        ServiceLoader<S> loader = ServiceLoader.load(service, _Context.getDefaultClassLoader());

        return _Sets.unmodifiable(loader);
    }

    /**
     * Uses application scoped caching. The first successful retrieval of a plugin for given
     * {@code pluginClass} is cached until application's life-cycle ends.
     *
     * @param pluginClass
     * @param onAmbiguity what to do if more than one matching plugin is found
     * @param onNotFound what to do if no matching plugin is found
     * @return
     */
    public static <S> S getOrElse(Class<S> pluginClass, Function<Set<S>, S> onAmbiguity, Supplier<S> onNotFound){

        // lookup cache first
        return _Context.computeIfAbsent(pluginClass, ()->{

            final Set<S> plugins = loadAll(pluginClass);

            if(plugins.isEmpty()) {
                return onNotFound.get();
            }

            if(plugins.size()>1) {
                return onAmbiguity.apply(plugins);
            }

            return plugins.iterator().next();

        });

    }

    // -- CONVENIENT PICK ANY

    public static <T> T pickAnyAndWarn(Class<T> pluginInterfaceClass, Set<T> ambiguousPlugins) {
        final Logger log = LogManager.getLogger(pluginInterfaceClass);
        final T any = ambiguousPlugins.iterator().next();

        log.warn(String.format("You have more than one plugin implementing '%s' on your class-path [%s], "
                + "just picking one: '%s'",
                pluginInterfaceClass.getName(),
                ambiguousPlugins.stream().map(p->p.getClass().getName()).collect(Collectors.joining(", ")),
                any.getClass().getName()
                ));

        return any;
    }

    // -- CONVENIENT EXCEPTION FACTORIES

    public static <T> PluginResolveException ambiguityNonRecoverable(
            Class<T> pluginInterfaceClass,
            Set<? extends T> ambiguousPlugins) {

        return new PluginResolveException(
                String.format("Ambiguous plugins implementing %s found on class path.\n{%s}",
                        pluginInterfaceClass.getName(),

                        _NullSafe.stream(ambiguousPlugins)
                        .map(Object::getClass)
                        .map(Class::getName)
                        .collect(Collectors.joining(", "))

                        ));
    }

    public static PluginResolveException absenceNonRecoverable(Class<?> pluginInterfaceClass) {

        return new PluginResolveException(
                String.format("No plugin implementing %s found on class path.",
                        pluginInterfaceClass.getName() ));
    }

    // -- JUNIT TEST SUPPORT

    /**
     * Loads a plugin by name and class-path. (Most likely used by JUnit Tests.)
     * @param pluginClass
     * @param classPath
     * @param pluginFullyQualifiedClassName
     * @return
     */
    public static <S> S load(Class<S> pluginInterfaceClass, File classPath, String pluginFullyQualifiedClassName) {

        try {

            ClassLoader parentCL = pluginInterfaceClass.getClassLoader();
            URL[] urls = {classPath.toURI().toURL()};
            
            try(URLClassLoader cl = URLClassLoader.newInstance(urls, parentCL)) {
                Class<S> pluginClass = _Casts.uncheckedCast(
                        cl.loadClass(pluginFullyQualifiedClassName));
                S plugin = pluginClass.newInstance();

                _Context.putSingleton(pluginInterfaceClass, plugin);
                
                return plugin;
            }

        } catch (Exception e) {
            throw new PluginResolveException(
                    String.format("Failed to load plugin '%s' implementing '%s' from path '%s'.",
                            pluginFullyQualifiedClassName,
                            pluginInterfaceClass.getName(),
                            classPath.getAbsolutePath()
                            ), e);
        }

    }

}
