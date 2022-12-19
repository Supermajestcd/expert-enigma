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

package org.apache.isis.viewer.html.action.edit;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.html.action.Action;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.ForwardRequest;
import org.apache.isis.viewer.html.request.Request;

public class RemoveItemFromCollection implements Action {

    @Override
    public void execute(final Request request, final Context context, final Page page) {
        final String objectId = request.getObjectId();
        final String elementId = request.getElementId();
        final String collectionField = request.getProperty();

        final ObjectAdapter target = context.getMappedObject(objectId);
        final ObjectAdapter element = context.getMappedObject(elementId);
        final ObjectSpecification specification = target.getSpecification();
        final OneToManyAssociation field = (OneToManyAssociation) specification.getAssociation(collectionField);
        field.removeElement(target, element);

        request.forward(ForwardRequest.viewObject(objectId, collectionField));
    }

    @Override
    public String name() {
        return "remove";
    }

}
