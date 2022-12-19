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
package org.apache.isis.client.kroviz.utils

object IconManager {
    private const val PREFIX = "fas fa-"   //TODO far fa- ???
    const val DEFAULT_ICON = PREFIX + "bolt"

    const val DANGER = "text-danger"
    const val DISABLED = "text-disabled"
    private const val NORMAL = "text-normal"
    const val OK = "text-ok"
    const val WARN = "text-warn"

    /* Merge with configuration values*/
    private val word2Icon = mapOf(
            "About" to "info",
            "Actions" to "ellipsis-v",
            "All" to "asterisk",
            "Blobs" to "cloud",
            "Burger" to "bars",
            "Chart" to "magic",
            "Close" to "times",
            "Configuration" to "wrench",
            "Connect" to "plug",
            "Console" to "terminal",
            "Create" to "plus",
            "Debug" to "bug",
            "Delete" to "trash",
            "Details" to "info-circle",
            "Diagram" to "project-diagram",
            "Download" to "download",
            "Factory" to "industry",
            "Featured" to "keyboard",
            "Featured Types" to "keyboard",
            "Edit" to "pencil",
            "Error Handling" to "bug",
            "Error" to "bug",
            "Event" to "road",
            "Experimental" to "flask",
            "Export" to "file-export",
            "Facet" to "gem",
            "Find" to "search",
            "Hierarchy" to "sitemap",
            "History" to "history",
            "Hsql" to "database",
            "Isis" to "ankh",
            "JEE/CDI" to "jedi",
            "List" to "list",
            "Location" to "map-marker",
            "Log" to "history",
            "Manager" to "manager",
            "Map" to "map",
            "Me" to "user",
            "Notification" to "bell",
            "Notifications" to "bell",
            "Object" to "cube",
            "Objects" to "cubes",
            "OK" to "check",
            "Open" to "book",
            "Other" to "asterisk",
            "Primitives" to "hashtag",
            "Prototyping" to "object-group",
            "Queen" to "chess-queen",
            "Run" to "rocket",
            "Save" to "file",
            "Security" to "lock",
            "Simple" to "cubes",
            "Switch" to "power-off",
            "Target" to "bullseye",
            "Text" to "font",
            "Toast" to "bread-slice", //comment-alt-plus/minus/exclamation
            "Toolbar" to "step-backward",
            "Tooltips" to "comment-alt",
            "Temporals" to "clock",
            "Trees" to "tree",
            "Types" to "typewriter",
            "Undo" to "undo",
            "Unknown" to "question",
            "Visualize" to "eye",
            "Wikipedia" to "wikipedia-w")

    fun find(query: String): String {
        if (query.startsWith("fa")) return query
        val actionTitle = Utils.deCamel(query)
        val mixedCaseList = actionTitle.split(" ")
        for (w in mixedCaseList) {
            val hit = word2Icon[w]
            if (hit != null) {
                return PREFIX + hit
            }
        }
        return DEFAULT_ICON
    }

    fun findStyleFor(actionName: String): Set<String> {
        return when (actionName) {
            "delete" -> setOf(DANGER)
            "undo" -> setOf(WARN)
            "save" -> setOf(OK)
            else -> setOf(NORMAL)
        }
    }

/*   isis.reflector.facet.cssClassFa.patterns
new.*:fa-plus,
add.*:fa-plus-square,
create.*:fa-plus,
update.*:fa-edit,
delete.*:fa-trash,
save.*:fa-floppy-o,
change.*:fa-edit,
edit.*:fa-pencil-square-o,
maintain.*:fa-edit,
remove.*:fa-minus-square,
copy.*:fa-copy,
move.*:fa-exchange,
first.*:fa-star,
find.*:fa-search,
lookup.*:fa-search,
search.*:fa-search,
view.*:fa-search,
clear.*:fa-remove,
previous.*:fa-step-backward,
next.*:fa-step-forward,
list.*:fa-list,
all.*:fa-list,
download.*:fa-download,
upload.*:fa-upload,
export.*:fa-download,
switch.*:fa-exchange,
import.*:fa-upload,
execute.*:fa-bolt,
run.*:fa-bolt,
calculate.*:fa-calculator,
verify.*:fa-check-circle,
refresh.*:fa-refresh,
install.*:fa-wrench,
stop.*:fa-stop,
terminate.*:fa-stop,
cancel.*:fa-stop,
discard.*:fa-trash-o,
pause.*:fa-pause,
suspend.*:fa-pause,
resume.*:fa-play,
renew.*:fa-repeat,
reset.*:fa-repeat,
categorise.*:fa-folder-open-o,
assign.*:fa-hand-o-right,
approve.*:fa-thumbs-o-up,
decline.*:fa-thumbs-o-down
    */
}
