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
package org.apache.isis.applib.mixins.layout;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.MixinConstants;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.resources._Resources;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Mixin(method="act")
@RequiredArgsConstructor
public class Object_openRestApi {

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_openRestApi> {}

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-external-link",
            position = ActionLayout.Position.PANEL_DROPDOWN
            )
    @MemberOrder(name = MixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "750.1")
    public LocalResourcePath act() {
        val bookmark = bookmarkService.bookmarkFor(holder);
        val objType = bookmark.getObjectType();
        val objId = bookmark.getIdentifier();
        val restfulPathIfAny = _Resources.getRestfulPathIfAny();

        return restfulPathIfAny!=null
                ? new LocalResourcePath(String.format(
                        "/%s/objects/%s/%s", restfulPathIfAny, objType, objId))
                        : new LocalResourcePath(String.format(
                                "/objects/%s/%s", objType, objId));
    }

    @Inject BookmarkService bookmarkService;
    @Inject SwaggerService swaggerService;

}
