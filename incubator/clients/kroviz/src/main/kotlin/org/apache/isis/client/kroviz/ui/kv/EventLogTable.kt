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

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.EventLogDetail
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px

class EventLogTable(val model: List<LogEntry>) : VPanel() {
    val tabulator: Tabulator<LogEntry>

    private val columns = listOf(
            ColumnDefinition(
                    download = false,
                    title = "",
                    field = "state",
                    width = "50",
                    headerMenu = EventLogTableMgr().buildTableMenu(this),
                    hozAlign = Align.CENTER,
                    formatterComponentFunction = { _, _, data ->
                        buildActionButton(data).onClick { EventLogDetail(data).open() }.apply {
                            margin = CssSize(-10, UNIT.px)
                        }
                    }
            ),
            ColumnDefinition<LogEntry>(
                    download = false,
                    title = "Title",
                    field = "title",
                    headerFilter = Editor.INPUT,
                    width = "450",
                    formatterComponentFunction = { _, _, data -> buildObjectButton(data) }
            ),
            ColumnDefinition("State", "state", width = "100", headerFilter = Editor.INPUT, download = false),
            ColumnDefinition("Method", "method", width = "100", headerFilter = Editor.INPUT, download = false),
            ColumnDefinition<LogEntry>(
                    download = false,
                    title = "# of Agg.",
                    field = "nOfAggregators",
                    headerFilter = Editor.INPUT,
                    width = "20"),
            ColumnDefinition("req.len", field = "requestLength", width = "100", hozAlign = Align.RIGHT, download = false),
            ColumnDefinition("resp.len", field = "responseLength", width = "100", hozAlign = Align.RIGHT, download = false),
            ColumnDefinition("cacheHits", field = "cacheHits", width = "100", hozAlign = Align.RIGHT, download = false),
            ColumnDefinition("duration", field = "duration", width = "100", hozAlign = Align.RIGHT, download = false),
            ColumnDefinition(
                    download = false,
                    title = "Created",
                    field = "createdAt",
                    sorter = Sorter.DATETIME,
                    formatter = Formatter.DATETIME,
                    formatterParams = obj { outputFormat = "HH:mm:ss.SSS" },
                    width = "100"),
            ColumnDefinition(
                    download = false,
                    title = "Updated",
                    field = "updatedAt",
                    sorter = Sorter.DATETIME,
                    formatter = Formatter.DATETIME,
                    formatterParams = obj { outputFormat = "HH:mm:ss.SSS" },
                    width = "100")
    )

    private fun buildObjectButton(data: LogEntry): Button {
        val b = Button(
                text = data.title,
                icon = data.state.iconName,
                style = ButtonStyle.LINK)
        b.onClick {
            console.log(data)
        }
        if (data.obj is TObject) b.setDragDropData(Constants.stdMimeType, data.url)
        return b
    }

    private fun buildActionButton(data: LogEntry): Button {
        val b = Button(
                text = "",
                icon = "fa fa-info-circle",
                style = data.state.style)
        return b
    }

    init {
        hPanel(FlexWrap.NOWRAP,
                alignItems = AlignItems.CENTER,
                spacing = 20) {
            border = Border(width = 1.px)
        }

        val options = TabulatorOptions(
                movableColumns = true,
                height = Constants.calcHeight,
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false
        )

        tabulator = tabulator(model, options = options) {
            setEventListener<Tabulator<LogEntry>> {
                tabulatorRowClick = {
                }
            }
        }
    }

}
