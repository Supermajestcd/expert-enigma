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
package demoapp.dom.types.clob;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.util.JaxbAdapters;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.Clob", editing=Editing.ENABLED)
@Log4j2
public class ClobDemo extends DemoStub {

    @Override
    public void initDefaults() {

        log.info("ClobDemo::initDefaults");

        try {
            val text = _Strings.read(_Resources.load(ClobDemo.class, "document.txt"), StandardCharsets.UTF_8);
            document = Clob.of("document", CommonMimeType.TXT, text);
        } catch (Exception e) {
            log.error("failed to create Clob from text resource", e);
        }

    }

    // -- EDITABLE

    @Property
    @PropertyLayout
    @XmlElement @XmlJavaTypeAdapter(JaxbAdapters.ClobAdapter.class)
    @Getter @Setter private Clob document;

    // -- READONLY

    //    @Property(editing=Editing.DISABLED)
    //    @XmlElement @Getter @Setter private Blob blobReadonly;
    //    
    //    @Property(editing=Editing.DISABLED)
    //    @PropertyLayout(multiLine=3)
    //    @XmlElement @Getter @Setter private String stringMultilineReadonly;

}
