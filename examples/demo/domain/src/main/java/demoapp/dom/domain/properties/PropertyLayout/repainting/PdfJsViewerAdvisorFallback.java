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
package demoapp.dom.domain.properties.PropertyLayout.repainting;

import org.springframework.stereotype.Service;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.Scale;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.spi.PdfJsViewerAdvisor;

@Service
public class PdfJsViewerAdvisorFallback implements PdfJsViewerAdvisor {

    @Override
    public Advice advise(InstanceKey instanceKey) {
        return new Advice(1, new Advice.TypeAdvice(Scale._1_00, 400));
    }

    @Override
    public void pageNumChangedTo(InstanceKey instanceKey, int pageNum) {
    }

    @Override
    public void scaleChangedTo(InstanceKey instanceKey, Scale scale) {
    }

    @Override
    public void heightChangedTo(InstanceKey instanceKey, int height) {
    }
}
