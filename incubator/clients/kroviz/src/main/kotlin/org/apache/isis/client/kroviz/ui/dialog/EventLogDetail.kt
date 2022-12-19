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
package org.apache.isis.client.kroviz.ui.dialog

import org.apache.isis.client.kroviz.core.aggregator.CollectionAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.LogEntryDecorator
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.diagram.JsonDiagram
import org.apache.isis.client.kroviz.ui.diagram.LayoutDiagram
import org.apache.isis.client.kroviz.utils.StringUtils
import org.apache.isis.client.kroviz.utils.XmlHelper

class EventLogDetail(val logEntryFromTabulator: LogEntry) : Command() {
    private var logEntry: LogEntry

    init {
        // For a yet unknown reason, aggregators are not transmitted via tabulator.
        // As a WORKAROUND, we fetch the full blown LogEntry from the EventStore again.
        val rs = ResourceSpecification(logEntryFromTabulator.title)
        logEntry = EventStore.findBy(rs)?: logEntryFromTabulator  // in case of xml, we use the entry passed in
    }

    // callback parameter
    private val LOG: String = "log"
    private val OBJ: String = "obj"

    fun open() {
        val responseStr = if (logEntry.subType == Constants.subTypeJson) {
            StringUtils.format(logEntry.response)
        } else {
            XmlHelper.format(logEntry.response)
        }

        val led = LogEntryDecorator(logEntry)
        val children = led.findChildren()
        var kids = ""
        children.forEach { kids += it.url + "\n" }
        var orphans = ""
        led.findOrphans(children).forEach { orphans += it + "\n" }
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", ValueType.TEXT, logEntry.title))
        formItems.add(FormItem("Response", ValueType.TEXT_AREA, responseStr, 10))
        formItems.add(FormItem("Aggregators", ValueType.TEXT, content = logEntry.aggregators))
        formItems.add(FormItem("Children", ValueType.TEXT_AREA, kids, size = 5))
        formItems.add(FormItem("Orphans", ValueType.TEXT_AREA, orphans, size = 5))
        formItems.add(FormItem("Object Diagram", ValueType.BUTTON, null, callBack = this, callBackAction = OBJ))
        formItems.add(FormItem("Console", ValueType.BUTTON, null, callBack = this, callBackAction = LOG))

        RoDialog(
                caption = "Details :" + logEntry.title,
                items = formItems,
                command = this,
                defaultAction = "Diagram",
                widthPerc = 60).open()
    }

    override fun execute(action: String?) {
        when {
            action.isNullOrEmpty() -> defaultAction()
            action == LOG -> {
                console.log(logEntry)
            }
            action == OBJ -> {
                objectDiagram()
            }
            else -> {
                console.log(logEntry)
                console.log("Action not defined yet: " + action)
            }
        }
    }

    private fun objectDiagram() {
        logEntry.aggregators.forEach {
            console.log(it)
            if (it is CollectionAggregator) {
                val displayModel = it.dpm
                // https://github.com/moll/json-stringify-safe/blob/master/stringify.js
                val jsonStr = JSON.stringify(displayModel)
                console.log(jsonStr)
                val pumlCode = JsonDiagram.build(jsonStr)
                DiagramDialog("Object Diagram", pumlCode).open()
            }
        }
    }

    private fun defaultAction() {
        val str = logEntry.response
        val pumlCode = when {
            str.startsWith("<") -> {
                val grid = logEntry.obj as Grid
                LayoutDiagram.build(grid)
            }
            str.startsWith("{") ->
                JsonDiagram.build(str)
            else -> "{}"
        }
        DiagramDialog("Response Diagram", pumlCode).open()
    }

}
