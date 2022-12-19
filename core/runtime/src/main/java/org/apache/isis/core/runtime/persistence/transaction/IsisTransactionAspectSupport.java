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
package org.apache.isis.core.runtime.persistence.transaction;

import java.util.Optional;

import org.apache.isis.core.commons.concurrent.AwaitableLatch;
import org.apache.isis.core.commons.internal.context._Context;

public final class IsisTransactionAspectSupport {

    public static void clearTransactionObject() {
        _Context.threadLocalClear(IsisTransactionObject.class);
    }

    public static void putTransactionObject(IsisTransactionObject txStatus) {
        _Context.threadLocalPut(IsisTransactionObject.class, txStatus);
    }

    public static Optional<IsisTransactionObject> currentTransactionObject() {
        return _Context.threadLocalGet(IsisTransactionObject.class)
                .getFirst();
    }

    public static boolean isTransactionInProgress() {
        return currentTransactionObject()
                .map(IsisTransactionObject::getCountDownLatch)
                .map(latch->latch.getCount()>0)
                .orElse(false);
    }

    public static AwaitableLatch transactionLatch() {
        return currentTransactionObject()
                .map(IsisTransactionObject::getCountDownLatch)
                .map(AwaitableLatch::of)
                .orElseGet(AwaitableLatch::unlocked);
    }

}
