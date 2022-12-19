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
package demoapp.dom.domain.properties.Property.commandPublishing.jdo;

import javax.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;

import demoapp.dom.domain.properties.Property.commandPublishing.PropertyCommandPublishingDisabledMetaAnnotation;
import demoapp.dom.domain.properties.Property.commandPublishing.PropertyCommandPublishingEnabledMetaAnnotation;
import demoapp.dom.domain.properties.Property.commandPublishing.PropertyCommandPublishingEntity;
import lombok.Getter;
import lombok.Setter;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.PropertyCommandPublishingEntity")
@DomainObject(
        nature=Nature.ENTITY
        , editing = Editing.ENABLED
)
public class PropertyCommandPublishingJdo
        extends PropertyCommandPublishingEntity {
    // ...
//end::class[]

    public PropertyCommandPublishingJdo(final String initialValue) {
        this.property = initialValue;
        this.propertyCommandPublishingDisabled = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    @ObjectSupport public String title() {
        return "Property#commandPublishing (JDO)";
    }

//tag::annotation[]
    @Property(
        commandPublishing = Publishing.ENABLED                  // <.>
    )
    @PropertyLayout(
        describedAs = "@Property(commandPublishing = ENABLED)",
        fieldSetId = "annotation", sequence = "1")
    @Getter @Setter
    private String property;
//end::annotation[]

//tag::annotation-2[]
    @Property(
        commandPublishing = Publishing.DISABLED                 // <.>
    )
    @PropertyLayout(
        describedAs = "@Property(commandPublishing = DISABLED)",
        fieldSetId = "annotation", sequence = "2")
    @Getter @Setter
    private String propertyCommandPublishingDisabled;
//end::annotation-2[]

//tag::meta-annotation[]
    @PropertyCommandPublishingEnabledMetaAnnotation               // <.>
    @PropertyLayout(
            describedAs = "@PropertyCommandEnabledMetaAnnotation",
            fieldSetId = "meta-annotated", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotated;
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @PropertyCommandPublishingDisabledMetaAnnotation              // <.>
    @Property(commandPublishing = Publishing.ENABLED)             // <.>
    @PropertyLayout(
        describedAs =
            "@PropertyCommandDisabledMetaAnnotation " +
            "@Property(commandPublishing = ENABLED)",
            fieldSetId = "meta-annotated-overridden", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::meta-annotation-overridden[]

//tag::class[]

}
//end::class[]
