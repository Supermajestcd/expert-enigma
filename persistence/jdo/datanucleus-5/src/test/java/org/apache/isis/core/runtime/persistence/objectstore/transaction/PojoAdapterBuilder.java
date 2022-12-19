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

package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import java.util.Iterator;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.persistence.jdo.datanucleus5.objectadapter.PojoAdapter;

import lombok.val;

public class PojoAdapterBuilder {

    private PojoAdapterBuilder(){
    }

    private Object pojo = new Object();

    private SpecificationLoader specificationLoader;

    private ObjectSpecId objectSpecId = ObjectSpecId.of("CUS");
    private String identifier = "1";
    // only used if type is AGGREGATED
    private String aggregatedId = "firstName";

    private Type type = Type.ROOT;
    private Persistence persistence = Persistence.PERSISTENT;

    public enum Persistence {
        PERSISTENT {
            @Override
            RootOid createOid(ObjectSpecId objectSpecId, String identifier) {
                return Factory.root(objectSpecId, identifier);
            }
        },
        VALUE {
            @Override
            RootOid createOid(ObjectSpecId objectSpecId, String identifier) {
                return null;
            }
        };
        abstract RootOid createOid(ObjectSpecId objectSpecId, String identifier);
    }

    public static enum Type {
        ROOT {
            @Override
            Oid oidFor(RootOid rootOid, ObjectSpecId objectSpecId, String unused) {
                return rootOid;
            }
        }, COLLECTION {
            @Override
            Oid oidFor(RootOid rootOid, ObjectSpecId objectSpecId, String collectionId) {
                return Oid.Factory.parentedForTesting(rootOid, collectionId);
            }
        }, VALUE {
            @Override
            Oid oidFor(RootOid rootOid, ObjectSpecId objectSpecId, String unused) {
                return null;
            }
        };

        abstract Oid oidFor(RootOid rootOid, ObjectSpecId objectSpecId, String supplementalId);
    }

    public static PojoAdapterBuilder create() {
        return new PojoAdapterBuilder();
    }

    public PojoAdapterBuilder withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public PojoAdapterBuilder withObjectType(String objectType) {
        this.objectSpecId = ObjectSpecId.of(objectType);
        return this;
    }

    public PojoAdapterBuilder withPojo(Object pojo) {
        this.pojo = pojo;
        return this;
    }

    //test only
    public PojoAdapterBuilder withOid(String oidAndTitle) {
        final Iterator<String> iterator = _Strings.splitThenStream(oidAndTitle, "|").iterator(); 

        if(!iterator.hasNext()) { return this; }
        withObjectType(iterator.next());
        if(!iterator.hasNext()) { return this; }
        withIdentifier(iterator.next());
        return this;
    }

    /**
     * A Persistence of VALUE implies a Type of VALUE also
     */
    public PojoAdapterBuilder with(Persistence persistence) {
        this.persistence = persistence;
        if(persistence == Persistence.VALUE) {
            this.type = Type.VALUE;
        }
        return this;
    }

    /**
     * A Type of VALUE implies a Persistence of VALUE also.
     */
    public PojoAdapterBuilder with(Type type) {
        this.type = type;
        if(type == Type.VALUE) {
            this.persistence = Persistence.VALUE;
        }
        return this;
    }

    public PojoAdapterBuilder with(SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
        return this;
    }

    public PojoAdapter build() {
        val rootOid = persistence.createOid(objectSpecId, identifier);
        val oid = type.oidFor(rootOid, objectSpecId, aggregatedId);
        val pojoAdapter = PojoAdapter.of(pojo, oid, specificationLoader);
        return pojoAdapter;
    }



}
