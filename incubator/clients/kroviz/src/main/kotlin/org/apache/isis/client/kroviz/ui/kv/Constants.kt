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
package org.apache.isis.client.kroviz.ui.kv

object Constants {

    const val stdMimeType = "text/plain"
    const val svgMimeType = "image/svg+xml"
    const val calcHeight = "calc(100vh - 88px)"
    const val actionSeparator = "\n"
    const val subTypeJson = "json"
    const val subTypeXml = "xml"

    //const val plantUmlUrl = "https://kroki.io/" //see: https://github.com/yuzutech/kroki
    const val plantUmlUrl = "http://localhost:8080/"
    //host:port depend on how docker is started
    // docker run -d --name kroki -p 8080:8000 yuzutech/kroki

}
