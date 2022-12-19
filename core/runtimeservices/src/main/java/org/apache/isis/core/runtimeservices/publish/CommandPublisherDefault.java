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
package org.apache.isis.core.runtimeservices.publish;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.core.metamodel.services.publishing.CommandPublisher;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@Named("isis.runtimeservices.CommandPublisherDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Internal")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class CommandPublisherDefault implements CommandPublisher {
    
    private final List<CommandSubscriber> subscribers;
    
    private Can<CommandSubscriber> enabledSubscribers;
    
    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    @Override
    public void complete(final @NonNull Command command) { 
        
        if(!canPublish()) {
            return;
        }

        if(!command.isPublishingEnabled()) {
            return;
        }
        if(command.getLogicalMemberIdentifier() == null) {
            // eg if seed fixtures
            return;
        }

        subscribers.forEach(subscriber -> subscriber.onCompleted(command));
    }
    
    // -- HELPER
    
    private boolean canPublish() {
        return enabledSubscribers.isNotEmpty();
    }
    

}

