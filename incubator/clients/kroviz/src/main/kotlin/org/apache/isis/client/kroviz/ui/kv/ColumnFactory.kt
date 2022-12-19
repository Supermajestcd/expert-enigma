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

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.core.model.ListDM
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.tabulator.Align
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Editor
import pl.treksoft.kvision.tabulator.Formatter
import pl.treksoft.kvision.tabulator.js.Tabulator
import pl.treksoft.kvision.utils.obj

/**
 * Create ColumnDefinitions for Tabulator tables
 */
class ColumnFactory {

    private val checkFormatterParams = obj {
        allowEmpty = true
        allowTruthy = true
        tickElement = "<i class='fa fa-square-o'></i>"
        crossElement = "<i class='fa fa-check-square-o'></i>"
    }

    private val menuFormatterParams = obj {
        allowEmpty = true
        allowTruthy = true
        tickElement = "<i class='fa fa-ellipsis-v'></i>"
        crossElement = "<i class='fa fa-ellipsis-v'></i>"
    }

    fun buildColumns(
            displayList: ListDM,
            withCheckBox: Boolean = false): List<ColumnDefinition<dynamic>> {

        val columns = mutableListOf<ColumnDefinition<dynamic>>()
        if (withCheckBox) {
            val checkBox = buildCheckBox()
            columns.add(checkBox)
        }

        val menu = buildMenu()
        columns.add(menu)

        val model = displayList.data as List<dynamic>
        if (model[0].hasOwnProperty("iconName") as Boolean) {
            val icon = buildLinkIcon()
            columns.add(icon)
        }

        val propertyLabels = displayList.propertyDescriptionList
        for (pl in propertyLabels) {
            val id = pl.key
            val friendlyName = pl.value
            var cd = ColumnDefinition<Exposer>(
                    title = friendlyName,
                    field = id,
                    headerFilter = Editor.INPUT)
            if (id == "object") {
                cd = buildLink()
            }
            columns.add(cd)
        }
        return columns
    }

    private fun buildLinkIcon(): ColumnDefinition<Exposer> {
        return ColumnDefinition<dynamic>(
                "",
                field = "iconName",
                hozAlign = Align.CENTER,
                width = "40",
                formatterComponentFunction = { _, _, data ->
                    buildButton(data, data["iconName"] as? String)
                })
    }

    private fun buildButton(data: Exposer, iconName: String?): Button {
        val tObject = data.delegate
        val b = Button(text = "", icon = iconName, style = ButtonStyle.LINK).onClick {
            UiManager.displayModel(tObject)
        }
        val logEntry = EventStore.find(tObject)!!
        b.setDragDropData(Constants.stdMimeType, logEntry.url)
        return b
    }


    private fun buildLink(): ColumnDefinition<Exposer> {
        return ColumnDefinition<dynamic>(
                title = "ResultListResult",
                field = "result",
                headerFilter = Editor.INPUT,
                formatterComponentFunction = { _, _, data ->
                    Button(text = data["object"].title as String, icon = "fas fa-star-o", style = ButtonStyle.LINK).onClick {
                        console.log(data)
                    }
                })
    }

    private fun buildCheckBox(): ColumnDefinition<Exposer> {
        return ColumnDefinition<Exposer>(
                title = "",
                field = "selected",
                formatter = Formatter.TICKCROSS,
                formatterParams = checkFormatterParams,
                /*               formatterComponentFunction = { cell, _, _ ->
                                   if (isSelected(cell)) {
                                       obj {"<i class='fa fa-check-square-o'></i>"}
                                   } else {
                                       obj {"<i class='fa fa-square-o'></i>"}
                                   }
                               }, */
                hozAlign = Align.CENTER,
                width = "40",
                headerSort = false,
                cellClick = { evt, cell ->
                    evt.stopPropagation()
                    toggleSelection(cell)
                })
    }

    private fun buildMenu(): ColumnDefinition<Exposer> {
        return ColumnDefinition("",
                field = "iconName", // any existing field can be used
                formatter = Formatter.TICKCROSS,
                formatterParams = menuFormatterParams,
                hozAlign = Align.CENTER,
                width = "60",
                headerSort = false,
                formatterComponentFunction = { _, _, data ->
                    val tObject = data.delegate
                    MenuFactory.buildForObject(
                            tObject,
                            false)
                })
    }

    private fun getData(cell: Tabulator.CellComponent): Exposer {
        val row = cell.getRow()
        val data = row.getData().asDynamic()
        return data as Exposer
    }

    private fun toggleSelection(cell: Tabulator.CellComponent) {
        val exposer = getData(cell)
        val oldValue = exposer.selected
        exposer.selected = !oldValue
    }

}
