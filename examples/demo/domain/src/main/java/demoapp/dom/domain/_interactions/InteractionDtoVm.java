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
package demoapp.dom.domain._interactions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.core.runtimeservices.urlencoding.UrlEncodingServiceWithCompression;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

//tag::class[]
@DomainObject(
    objectType = "demo.InteractionDtoVm"
    , nature = Nature.VIEW_MODEL
)
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDtoVm implements ViewModel {

    private final static UrlEncodingService encodingService = new UrlEncodingServiceWithCompression();

    public String title() {
        // nb: not thread-safe
        // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        val exec = getInteractionDto().getExecution();
        val instant = exec.getMetrics().getTimings().getStartedAt().toGregorianCalendar().toInstant();
        
        val buf = new TitleBuffer();
        buf.append(format.format(Date.from(instant)));
        buf.append(" ").append(exec.getLogicalMemberIdentifier());
        return buf.toString();
    }


    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE)
    @Getter @Setter
    private InteractionDto interactionDto;

    @Override
    public String viewModelMemento() {
        return encodingService.encodeString(InteractionDtoUtils.toXml(interactionDto));
    }

    @Override
    public void viewModelInit(String memento) {
        interactionDto =  InteractionDtoUtils.fromXml(encodingService.decodeToString(memento));
    }

}
//end::class[]
