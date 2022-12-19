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

package org.apache.isis.viewer.dnd.icon;

import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;

public class IconSpecification implements ViewSpecification {
    private final boolean isSubView;
    private final boolean isReplaceable;

    public IconSpecification() {
        this(true, true);
    }

    IconSpecification(final boolean isSubView, final boolean isReplaceable) {
        this.isSubView = isSubView;
        this.isReplaceable = isReplaceable;
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.isClosed() && requirement.isObject() && requirement.hasReference();
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        final Text style = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
        final Icon icon = new Icon(content, this);
        icon.setTitle(new ObjectTitleText(icon, style));
        icon.setSelectedGraphic(new IconGraphic(icon, style));
        return icon;
    }

    @Override
    public String getName() {
        return "Icon";
    }

    @Override
    public boolean isSubView() {
        return isSubView;
    }

    @Override
    public boolean isReplaceable() {
        return isReplaceable;
    }

    @Override
    public boolean isResizeable() {
        return false;
    }

    public View decorateSubview(final View subview) {
        return subview;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isAligned() {
        return false;
    }
}
