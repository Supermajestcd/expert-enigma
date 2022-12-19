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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.commons.internal.encoding.DataInputExtended;
import org.apache.isis.commons.internal.encoding.DataOutputExtended;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.val;

public class StandaloneData extends Data {

    private static final long serialVersionUID = 1L;

    private static enum As {
        
        ENCODED_STRING(0), 
        SERIALIZABLE(1);
        
        static Map<Integer, As> cache = new HashMap<Integer, As>();
        static {
            for (final As as : values()) {
                cache.put(as.idx, as);
            }
        }
        private final int idx;

        private As(int idx) {
            this.idx = idx;
        }

        static As get(int idx) {
            return cache.get(idx);
        }

        public static As readFrom(DataInputExtended input) throws IOException {
            return get(input.readByte());
        }

        public void writeTo(DataOutputExtended output) throws IOException {
            output.writeByte(idx);
        }
    }

    private String objectAsEncodedString;
    private Serializable objectAsSerializable;

    public StandaloneData(ManagedObject adapter) {
        super(null, adapter.getSpecification().getFullIdentifier());

        final Object object = adapter.getPojo();
        if (object instanceof Serializable) {
            this.objectAsSerializable = (Serializable) object;
            initialized();
            return;
        }

        val encodeableFacet = adapter.getSpecification().getFacet(EncodableFacet.class);
        if (encodeableFacet != null) {
            this.objectAsEncodedString = encodeableFacet.toEncodedString(adapter);
            initialized();
            return;
        }

        throw new IllegalArgumentException("Object wrapped by standalone adapter is not serializable and its specificatoin does not have an EncodeableFacet");
    }

    public StandaloneData(final DataInputExtended input) throws IOException {
        super(input);
        final As as = As.readFrom(input);
        if (as == As.SERIALIZABLE) {
            this.objectAsSerializable = input.readSerializable(Serializable.class);
        } else {
            this.objectAsEncodedString = input.readUTF();
        }
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        if (objectAsSerializable != null) {
            As.SERIALIZABLE.writeTo(output);
            output.writeSerializable(objectAsSerializable);
        } else {
            As.ENCODED_STRING.writeTo(output);
            output.writeUTF(objectAsEncodedString);
        }
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////

    public ObjectAdapter getAdapter(
            ObjectAdapterProvider objectAdapterProvider,
            SpecificationLoader specificationLoader) {
        
        if (objectAsSerializable != null) {
            return objectAdapterProvider.adapterFor(objectAsSerializable);
        } else {
            val spec = 
                    specificationLoader.lookupBySpecIdElseLoad(ObjectSpecId.of(getClassName()));
            val encodeableFacet = spec.getFacet(EncodableFacet.class);
            return encodeableFacet.fromEncodedString(objectAsEncodedString);
        }
    }

}
