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
package org.apache.isis.valuetypes.asciidoc.ui.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent_reloadJs;

import lombok.val;

public class AsciiDocComponent extends MarkupComponent {

    private static final long serialVersionUID = 1L;

    public AsciiDocComponent(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        val htmlContent = extractHtmlOrElse(getDefaultModelObject(), "" /*fallback*/);
        replaceComponentTagBody(markupStream, openTag, 
                MarkupComponent_reloadJs.decorate(htmlContent, jsRef.get()));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(
                new CssResourceReference(AsciiDocComponent.class, "css/prism.css")));

        response.render(JavaScriptHeaderItem.forReference(jsRef.get()));

    }

    private static final _Lazy<JavaScriptResourceReference> jsRef = _Lazy.threadSafe(()->
    new JavaScriptResourceReference(AsciiDocComponent.class, "js/prism1.14.js"));

}
