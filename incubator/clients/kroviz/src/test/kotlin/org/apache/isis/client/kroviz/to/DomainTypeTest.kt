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
package org.apache.isis.client.kroviz.to

import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.FILE_NODE
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomainTypeTest {

    @Test
    fun testParseSimpleObject() {
        // given
        val jsonStr = SO.str
        // when
        val domainType = Json.decodeFromString(DomainType.serializer(), jsonStr)
        // then
        val linkList = domainType.links
        assertEquals(2, linkList.size)

        assertEquals("domainapp.modules.simple.dom.impl.SimpleObject", domainType.canonicalName)

        val members = domainType.members
        assertEquals(9, members.size)

        val typeActions = domainType.typeActions
        assertEquals(2, typeActions.size)

        assertNotNull(domainType.extensions)
    }

    @Test
    fun testParseFileNode() {
        // given
        val jsonStr = FILE_NODE.str
        // when
        val domainType = Json.decodeFromString(DomainType.serializer(), jsonStr)
        // then
        val linkList = domainType.links
        assertEquals(2, linkList.size)

        assertEquals("demoapp.dom.tree.FileNode", domainType.canonicalName)

        val members = domainType.members
        assertEquals(8, members.size)

        val typeActions = domainType.typeActions
        assertEquals(2, typeActions.size)

        assertNotNull(domainType.extensions)
    }

}
