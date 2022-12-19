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
package demoapp.dom.annotLayout.DomainObjectLayout;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Blob;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.annotLayout.PropertyLayout.cssClass.PropertyLayoutCssClassVm;
import demoapp.dom.annotLayout.PropertyLayout.describedAs.PropertyLayoutDescribedAsVm;
import demoapp.dom.annotLayout.PropertyLayout.hidden.PropertyLayoutHiddenVm;
import demoapp.dom.annotLayout.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;
import demoapp.dom.annotLayout.PropertyLayout.labelPosition.PropertyLayoutLabelPositionVm;
import demoapp.dom.annotLayout.PropertyLayout.multiLine.PropertyLayoutMultiLineVm;
import demoapp.dom.annotLayout.PropertyLayout.named.PropertyLayoutNamedVm;
import demoapp.dom.annotLayout.PropertyLayout.navigable.FileNodeVm;
import demoapp.dom.annotLayout.PropertyLayout.renderDay.PropertyLayoutRenderDayVm;
import demoapp.dom.annotLayout.PropertyLayout.repainting.PropertyLayoutRepaintingVm;
import demoapp.dom.annotLayout.PropertyLayout.typicalLength.PropertyLayoutTypicalLengthVm;
import demoapp.dom.types.Samples;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.DomainObjectLayoutMenu")
@Log4j2
public class DomainObjectLayoutMenu {


}
