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
package org.apache.isis.runtime.services.command;

import javax.inject.Singleton;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandDefault;
import org.apache.isis.applib.services.command.spi.CommandService;

@Singleton
public class CommandServiceDefault implements CommandService {

    @Override
    public Command create() {
        return new CommandDefault();
    }

    @Override
    public void complete(final Command command) {
        // nothing to do
    }

    @Override
    public boolean persistIfPossible(final Command command) {
        return false;
    }

}
