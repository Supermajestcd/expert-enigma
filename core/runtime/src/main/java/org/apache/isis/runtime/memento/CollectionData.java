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

package org.apache.isis.runtime.memento;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.encoding.DataOutputExtended;
import org.apache.isis.metamodel.adapter.oid.Oid;

public class CollectionData extends Data {

    private final static long serialVersionUID = 1L;
    private final Data[] elements;

    public CollectionData(final Oid oid, final String className, final Data[] elements) {
        super(oid, className);
        this.elements = elements;
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeEncodables(elements);
    }

    @Override
    public String toString() {
        return "(" + streamElements()
            .map(data->""+data)
            .collect(Collectors.joining(",")) + ")";
    }
    
    public Stream<Data> streamElements() {
        return _NullSafe.stream(elements);
    }
    
    public int getElementCount() {
        return _NullSafe.size(elements);
    }
    
}
