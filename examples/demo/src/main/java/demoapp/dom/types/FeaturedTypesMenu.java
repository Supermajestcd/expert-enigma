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
package demoapp.dom.types;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.val;

import demoapp.dom.types.blob.BlobDemo;
import demoapp.dom.types.clob.ClobDemo;
import demoapp.dom.types.markup.MarkupDemo;
import demoapp.dom.types.primitive.PrimitivesDemo;
import demoapp.dom.types.text.TextDemo;
import demoapp.dom.types.time.TemporalDemo;
import demoapp.dom.types.uuid.UuidDemo;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.FeaturedTypesMenu")
@DomainObjectLayout(named="Featured Types")
public class FeaturedTypesMenu {

    @Inject private FactoryService factoryService;

    @Action
    @ActionLayout(cssClassFa="fa-font")
    public TextDemo text(){
        val demo = factoryService.viewModel(TextDemo.class);
        demo.initDefaults();  
        return demo;
    }
    
    @Action
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitivesDemo primitives(){
        val demo = factoryService.viewModel(PrimitivesDemo.class);
        demo.initDefaults();  
        return demo;
    }

    @Action
    @ActionLayout(cssClassFa="fa-clock-o")
    public TemporalDemo temporals(){
        val demo = factoryService.viewModel(TemporalDemo.class);
        demo.initDefaults();  
        return demo;
    }
    
    @Action
    @ActionLayout(cssClassFa="fa-at")
    public UuidDemo uuid(){
        val demo = factoryService.viewModel(UuidDemo.class);
        demo.initDefaults();  
        return demo;
    }

    @Action
    @ActionLayout(cssClassFa="fa-cloud")
    public BlobDemo blobs(){
        val demo = factoryService.viewModel(BlobDemo.class);
        demo.initDefaults();  
        return demo;
    }
    
    @Action
    @ActionLayout(cssClassFa="fa-cloud")
    public ClobDemo clobs(){
        val demo = factoryService.viewModel(ClobDemo.class);
        demo.initDefaults();  
        return demo;
    }
    
    @Action
    @ActionLayout(cssClassFa="fa-code")
    public MarkupDemo markup(){
        val demo = factoryService.viewModel(MarkupDemo.class);
        demo.initDefaults();  
        return demo;
    }

}
