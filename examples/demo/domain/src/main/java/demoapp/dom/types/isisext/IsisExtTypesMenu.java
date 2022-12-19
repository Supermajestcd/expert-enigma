
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
package demoapp.dom.types.isisext;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.extern.log4j.Log4j2;

import demoapp.dom.types.isisext.asciidocs.IsisAsciiDocs;
import demoapp.dom.types.isisext.markdowns.IsisMarkdowns;

@DomainService(nature=NatureOfService.VIEW, logicalTypeName = "demo.IsisExtTypesMenu")
@DomainObjectLayout(named="IsisExtTypes")
@Log4j2
public class IsisExtTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-fancy")
    public IsisAsciiDocs asciiDocs(){
        return new IsisAsciiDocs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-fancy")
    public IsisMarkdowns markdowns(){
        return new IsisMarkdowns();
    }

}
