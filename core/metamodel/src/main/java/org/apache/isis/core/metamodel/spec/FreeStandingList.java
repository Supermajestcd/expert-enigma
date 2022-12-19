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

package org.apache.isis.core.metamodel.spec;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * A list returned from an action, ie not associated or owned by any entity.
 */
public class FreeStandingList extends AbstractList<ObjectAdapter> {

    private final List<ObjectAdapter> instances;
    private final ObjectSpecification instanceSpecification;

    public FreeStandingList(final ObjectSpecification instanceSpecification, final List<ObjectAdapter> instances) {
        this.instanceSpecification = instanceSpecification;
        this.instances = Collections.unmodifiableList(instances);
    }

    /**
     * Required implementation of {@link AbstractList}.
     */
    @Override
    public ObjectAdapter get(final int index) {
        return instances.get(index);
    }

    /**
     * Required implementation of {@link AbstractList}.
     */
    @Override
    public int size() {
        return instances.size();
    }

    public ObjectSpecification getElementSpecification() {
        return instanceSpecification;
    }

    public String titleString() {
        return instanceSpecification.getPluralName() + ", " + size();
    }

    @Override
    public String toString() {
        final ToString s = new ToString(this);
        s.append("elements", instanceSpecification.getFullIdentifier());

        // title
        String title;
        try {
            title = "'" + this.titleString() + "'";
        } catch (final NullPointerException e) {
            title = "none";
        }
        s.append("title", title);

        s.append("vector", instances);

        return s.toString();
    }

}
