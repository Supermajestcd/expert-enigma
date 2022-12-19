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
package org.apache.isis.applib.services.queryresultscache;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.DisposableBean;

import lombok.Data;
import lombok.Getter;


/**
 * Provides a mechanism by which idempotent query results can be cached for
 * the duration of an interaction.
 *
 * <p>
 * Caching such values is useful to improve the response time (for the end user)
 * of code that loops &quot;naively&quot; through a set of items, performing
 * an expensive operation each time.  If the data is such that the same
 * expensive operation is made many times, then the query cache is a perfect fit.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface QueryResultsCache extends DisposableBean {

    /**
     * Executes the callable if not already cached for the supplied calling
     * class, method and keys.
     *
     * @param callable
     * @param callingClass
     * @param methodName
     * @param keys
     * @param <T>
     * @return
     */
    <T> T execute(
            Callable<T> callable,
            Class<?> callingClass, String methodName,
            Object... keys);


    /**
     * Not API, for framework use only.
     */
    void onTransactionEnded();

    <R> R execute(
            MethodReferences.Call0<? extends R> action,
            Class<?> callingClass, String methodName);

    <R, A0> R execute(
            MethodReferences.Call1<? extends R, A0> action,
            Class<?> callingClass, String methodName,
            A0 arg0);

    <R, A0, A1> R execute(
            MethodReferences.Call2<? extends R, A0, A1> action,
            Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1);

    <R, A0, A1, A2> R execute(
            MethodReferences.Call3<? extends R, A0, A1, A2> action,
            Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1, A2 arg2);

    <R, A0, A1, A2, A3> R execute(
            MethodReferences.Call4<? extends R, A0, A1, A2, A3> action,
            Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1, A2 arg2, A3 arg3);

    <R, A0, A1, A2, A3, A4> R execute(
            MethodReferences.Call5<? extends R, A0, A1, A2, A3, A4> action,
            Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);

    class Key {

        @Getter
        private final Class<?> callingClass;
        @Getter
        private final String methodName;
        @Getter
        private final Object[] keys;

        public Key(Class<?> callingClass, String methodName, Object... keys) {
            this.callingClass = callingClass;
            this.methodName = methodName;
            this.keys = keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;

            // compare callingClass
            if (callingClass == null) {
                if (other.callingClass != null)
                    return false;
            } else if (!callingClass.equals(other.callingClass))
                return false;

            // compare methodName
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;

            // compare keys
            if (!Arrays.equals(keys, other.keys))
                return false;

            // ok, matches
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((callingClass == null) ? 0 : callingClass.hashCode());
            result = prime * result + Arrays.hashCode(keys);
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return callingClass.getName() + "#" + methodName  + Arrays.toString(keys);
        }
    }

    @Data
    class Value<T> {
        private final T result;
    }

}
