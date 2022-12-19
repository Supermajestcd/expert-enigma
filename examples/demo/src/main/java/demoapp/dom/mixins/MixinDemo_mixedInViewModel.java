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
package demoapp.dom.mixins;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.value.Markup;

import lombok.RequiredArgsConstructor;
import lombok.val;

//TODO does not work neither when inline or sidebar/model, disabled for now
//@Action//(associateWith = "note")
//@ActionLayout(named = "Update Note (with Help)")
//@DomainObject( // <-- activates the help extension below
//        nature=Nature.VIEW_MODEL, 
//        objectType = "demo.MixinDemo_mixedInViewModel") 
@RequiredArgsConstructor
public class MixinDemo_mixedInViewModel {
    
    private final MixinDemo holder;
    
    public MixinDemo act(String newNote) {
        holder.setNote(newNote);
        return holder;
    }
    
    public String default0Act() {
        return holder.getNote();
    }
    
    // -- USER HELP EXTENSION
    
    @Property
    public Markup getHelp() {
        
        val html = "<h1>Help</h2>"
                + "<p>Here we could fill in some advisory text to get you started.</p>";
        return new Markup(html);
    }
    
}
 