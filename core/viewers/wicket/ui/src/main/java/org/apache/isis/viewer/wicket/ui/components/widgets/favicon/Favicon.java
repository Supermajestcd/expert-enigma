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
package org.apache.isis.viewer.wicket.ui.components.widgets.favicon;

import javax.inject.Inject;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.viewer.wicket.ui.WebAppConfiguration;

/**
 * A component for application favorite icon
 */
public class Favicon extends WebComponent {

    private static final long serialVersionUID = 1L;

    @Inject private transient WebAppConfiguration webAppConfigBean;

    private String url;
    private String contentType;
    
    public Favicon(String id) {
        super(id);
        if(webAppConfigBean!=null) {
            url = webAppConfigBean.getFaviconUrl();
            contentType = webAppConfigBean.getFaviconContentType();
        }
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(!Strings.isEmpty(url));
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        tag.put("href", url);

        if (!Strings.isEmpty(contentType)) {
            tag.put("type", contentType);
        }
    }
}
