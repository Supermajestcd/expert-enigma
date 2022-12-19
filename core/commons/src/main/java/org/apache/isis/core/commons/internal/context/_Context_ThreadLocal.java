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

import java.util.Map;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.collections._Multimaps;
import org.apache.isis.core.commons.internal.base._With;

import lombok.Value;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Package private mixin for _Context. 
 * Provides a context for storing and retrieving thread local object references.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0
 * 
 */
final class _Context_ThreadLocal {

    // -- MIXINS

    @Value(staticConstructor = "of")
    private static final class ThreadKey {
        long threadId;
        int threadHashCode;
        static ThreadKey of(Thread thread) {
            return of(thread.getId(), thread.hashCode());
        }
    }

    static <T> Runnable put(Class<? super T> type, T variant) {
        _With.requires(type, "type");
        _With.requires(variant, "variant");

        val threadLocalMap = getOrCreateThreadLocalMap();
        threadLocalMap
        .compute(type, (k, v) -> v == null 
        ? Can.<T>ofSingleton(variant)
                : Can.<T>concat(_Casts.uncheckedCast(v), variant));

        val key = THREAD_LOCAL_MAP_KEY.get();

        return ()->{MAPS_BY_KEY.remove(key);};
    }

    static <T> Can<T> select(Class<? super T> type, Class<? super T> instanceOf) {
        val bin = _Context_ThreadLocal.<T>get(type);
        return bin.filter(t -> isInstanceOf(t, instanceOf));
    }

    private static boolean isInstanceOf(Object obj, Class<?> type) {
        return type.isAssignableFrom(obj.getClass());
    }

    static <T> Can<T> get(Class<? super T> type) {
        val threadLocalMap = getThreadLocalMap();
        if(threadLocalMap==null) {
            return Can.empty();
        }
        val bin = threadLocalMap.get(type);
        if(bin==null) {
            return Can.empty();	
        }
        return _Casts.uncheckedCast(bin);
    }

    static void clear(Class<?> type) {
        val threadLocalMap = getThreadLocalMap();
        if(threadLocalMap==null) {
            return;
        }
        threadLocalMap.remove(type);
    }

    static void cleanupThread() {
        val key = THREAD_LOCAL_MAP_KEY.get();
        THREAD_LOCAL_MAP_KEY.remove();
        MAPS_BY_KEY.remove(key);
    }

    // -- HELPER

    private _Context_ThreadLocal(){}

    static void clear() {
        MAPS_BY_KEY.clear();
    }

    //	/**
    //	 * Inheritable... allows to have concurrent computations utilizing the ForkJoinPool.
    //	 */
    //    private static final ThreadLocal<Map<Class<?>, Bin<?>>> THREAD_LOCAL_MAP = 
    //    		InheritableThreadLocal.withInitial(HashMap::new);

    /**
     * Inheritable... allows to have concurrent computations utilizing the ForkJoinPool.
     */
    private static final ThreadLocal<ThreadKey> THREAD_LOCAL_MAP_KEY = 
            InheritableThreadLocal.withInitial(()->ThreadKey.of(Thread.currentThread()));


    private static final _Multimaps.MapMultimap<ThreadKey, Class<?>, Can<?>> MAPS_BY_KEY =
            _Multimaps.newConcurrentMapMultimap(); 

    private static Map<Class<?>, Can<?>> getThreadLocalMap() {
        val key = THREAD_LOCAL_MAP_KEY.get(); // non-null
        val threadLocalMap = MAPS_BY_KEY.get(key); // might be null
        return threadLocalMap;
    }

    private static Map<Class<?>, Can<?>> getOrCreateThreadLocalMap() {
        val key = THREAD_LOCAL_MAP_KEY.get(); // non-null
        val threadLocalMap = MAPS_BY_KEY.get(key); // might be null
        if(threadLocalMap!=null) {
            return threadLocalMap;
        }
        val map = _Maps.<Class<?>, Can<?>>newHashMap();
        MAPS_BY_KEY.put(key, map);
        return map;
    }

}
