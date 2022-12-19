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
package demoapp.dom.annotDomain.Property.command;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.annotDomain._commands.ExposePersistedCommands;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.JDO_ENTITY
        , objectType = "demo.PropertyCommandJdo"
        , editing = Editing.ENABLED
)
public class PropertyCommandJdo
        implements HasAsciiDocDescription, ExposePersistedCommands {
    // ...
//end::class[]

    public PropertyCommandJdo(String initialValue) {
        this.property = initialValue;
        this.propertyCommandDisabled = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    public String title() {
        return "Property#command";
    }

//tag::annotation[]
    @Property(
        command = CommandReification.ENABLED             // <.>
    )
    @PropertyLayout(
        describedAs = "@Property(command = ENABLED)"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @Getter @Setter
    private String property;
//end::annotation[]

//tag::annotation-2[]
    @Property(
        command = CommandReification.DISABLED           // <.>
    )
    @PropertyLayout(
        describedAs = "@Property(command = DISABLED)"
    )
    @MemberOrder(name = "annotation", sequence = "2")
    @Getter @Setter
    private String propertyCommandDisabled;
//end::annotation-2[]

//tag::meta-annotation[]
    @PropertyCommandEnabledMetaAnnotation               // <.>
    @PropertyLayout(
            describedAs = "@PropertyCommandEnabledMetaAnnotation"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotated;
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @PropertyCommandDisabledMetaAnnotation              // <.>
    @Property(command = CommandReification.ENABLED)     // <.>
    @PropertyLayout(
        describedAs =
            "@PropertyCommandDisabledMetaAnnotation " +
            "@Property(command = ENABLED)"
    )
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::meta-annotation-overridden[]

//tag::class[]

}
//end::class[]
