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
package org.apache.isis.client.kroviz.core.event

import org.apache.isis.client.kroviz.ui.kv.Constants
import org.apache.isis.client.kroviz.utils.Utils

class ResourceSpecification(
        val url: String,
        val subType: String = Constants.subTypeJson) {

    fun isRedundant(): Boolean {
        return when {
            url.contains("object-layout") -> true
            url.contains("/properties/") -> true
            else -> false
        }
    }

    fun matches(logEntry: LogEntry): Boolean {
        return subType.equals(logEntry.subType)
                && areEquivalent(url, logEntry.url)
    }

    private fun areEquivalent(searchUrl: String, compareUrl: String, allowedDiff: Int = 1): Boolean {
        val sl = Utils.removeHexCode(searchUrl)
        val cl = Utils.removeHexCode(compareUrl)
        val searchList: List<String> = sl.split("/")
        val compareList: List<String> = cl.split("/")
        if (compareList.size != searchList.size) {
            return false
        }

        var diffCnt = 0
        for ((i, s) in searchList.withIndex()) {
            val c = compareList[i]
            if (s != c) {
                diffCnt++
                val n = s.toIntOrNull()
                // if the difference is a String, it is not allowed and counts double
                if (n == null) {
                    diffCnt++
                }
            }
        }
        return diffCnt <= allowedDiff
    }

}
