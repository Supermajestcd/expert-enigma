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

package org.apache.isis.viewer.wicket.viewer.services.mementos;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

final class CollectionData extends Data {

    private final static long serialVersionUID = 1L;
    private final Data[] elements;

    public CollectionData(final Oid oid, final Data[] elements) {
        super(oid);
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "(" 
                + streamElements()
                    .map(data->""+data)
                    .collect(Collectors.joining(",")) 
                + ")";
    }

    public Stream<Data> streamElements() {
        return _NullSafe.stream(elements);
    }

    public int getElementCount() {
        return _NullSafe.size(elements);
    }

}
