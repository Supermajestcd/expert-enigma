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
package org.apache.isis.core.unittestsupport.jmocking;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public final class IsisActions {

    private IsisActions() {
    }

    public static Action injectInto() {
        return InjectIntoJMockAction.injectInto();
    }

    @SafeVarargs
    public static <T> Action returnEach(final T... values) {
        return JMockActions.returnEach(values);
    }

    public static Action returnArgument(final int i) {
        return JMockActions.returnArgument(i);
    }

    public static Action returnNewTransientInstance() {
        return new Action(){

            @Override
            public void describeTo(Description description) {
                description.appendText("new transient instance");
            }

            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                Class<?> cls = (Class<?>) invocation.getParameter(0);
                return cls.newInstance();
            }
        };
    }


}
