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
package org.apache.isis.runtime.services.xmlsnapshot;

import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.util.snapshot.XmlSchema;
import org.apache.isis.metamodel.util.snapshot.XmlSnapshot;
import org.apache.isis.runtime.system.context.IsisContext;

/**
 * Builds an {@link XmlSnapshot} using a fluent use through a builder:
 *
 * <pre>
 * XmlSnapshot snapshot = XmlSnapshotBuilder.create(customer).includePath(&quot;placeOfBirth&quot;).includePath(&quot;orders/product&quot;).build();
 * Element customerAsXml = snapshot.toXml();
 * </pre>
 */
public class XmlSnapshotBuilder {

    private final Object domainObject;
    private XmlSchema schema;

    static class PathAndAnnotation {
        public PathAndAnnotation(final String path, final String annotation) {
            this.path = path;
            this.annotation = annotation;
        }

        private final String path;
        private final String annotation;
    }

    private final List<XmlSnapshotBuilder.PathAndAnnotation> paths = _Lists.newArrayList();

    public XmlSnapshotBuilder(final Object domainObject) {
        this.domainObject = domainObject;
    }

    public XmlSnapshotBuilder usingSchema(final XmlSchema schema) {
        this.schema = schema;
        return this;
    }

    public XmlSnapshotBuilder includePath(final String path) {
        return includePathAndAnnotation(path, null);
    }

    public XmlSnapshotBuilder includePathAndAnnotation(final String path, final String annotation) {
        paths.add(new PathAndAnnotation(path, annotation));
        return this;
    }

    public XmlSnapshot build() {
        final ObjectAdapter adapter = IsisContext.pojoToAdapter().apply(domainObject);
        final XmlSnapshot snapshot = (schema != null) ? new XmlSnapshot(adapter, schema) : new XmlSnapshot(adapter);
        for (final XmlSnapshotBuilder.PathAndAnnotation paa : paths) {
            if (paa.annotation != null) {
                snapshot.include(paa.path, paa.annotation);
            } else {
                snapshot.include(paa.path);
            }
        }
        return snapshot;
    }


}