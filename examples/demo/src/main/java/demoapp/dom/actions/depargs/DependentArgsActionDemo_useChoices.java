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
package demoapp.dom.actions.depargs;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.RequiredArgsConstructor;

@ActionLayout(named="Choices", promptStyle = PromptStyle.DIALOG_MODAL)
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DependentArgsActionDemo_useChoices {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;

    public DependentArgsActionDemo act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY)
            Parity parity,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            DemoItem item

            ) {

        messageService.informUser(item.getName());
        return holder;
    }

    // -- PARAM 1 (DemoItem)

    @Model
    public Collection<DemoItem> choices1Act(
            
            Parity parity // <-- the refining parameter from the dialog above
            
            ) {
        
        if(parity == null) {
            return holder.getItems();
        }
        return holder.getItems()
                .stream()
                .filter(item->parity == item.getParity())
                .collect(Collectors.toList());
    }


}

