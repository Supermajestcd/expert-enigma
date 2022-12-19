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
package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.to.User
import org.apache.isis.client.kroviz.to.Version

class SystemDM(override val title: String) : DisplayModel() {
    var user: User? = null
    var version: Version? = null
    private var domainTypes: DomainTypes? = null

    override fun canBeDisplayed(): Boolean {
        return !isRendered
    }

    override fun addData(obj: TransferObject) {
        when (obj) {
            is User -> user = obj
            is Version -> version = obj
            is DomainTypes -> domainTypes = obj
            else -> {
            }
        }
    }

}
