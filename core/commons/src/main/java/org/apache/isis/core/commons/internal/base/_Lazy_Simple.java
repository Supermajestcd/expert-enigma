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

package org.apache.isis.core.commons.internal.base;

import java.util.function.Supplier;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import static org.apache.isis.core.commons.internal.base._With.requires;

/**
 * package private mixin for _Lazy 
 * @since 2.0
 */
final class _Lazy_Simple<T> implements _Lazy<T> {

    private final Supplier<? extends T> supplier;
    private T value;
    private boolean memoized;

    _Lazy_Simple(Supplier<? extends T> supplier) {
        this.supplier = requires(supplier, "supplier");
    }

    @Override
    public boolean isMemoized() {
        return memoized;
    }

    @Override
    public void clear() {
        this.memoized = false;
        this.value = null;
    }

    @Override
    public T get() {
        if(memoized) {
            return value;
        }
        memoized = true;
        return value = supplier.get();
    }

    @Override
    public void set(T value) {
        if(memoized) {
            throw _Exceptions.illegalState("cannot set value '%s' on Lazy that has already memoized a value", ""+value);
        }
        memoized = true;
        this.value = value;
    }

}
