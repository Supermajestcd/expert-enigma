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

import io.kvision.panel.SimplePanel
import io.kvision.state.observableListOf
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.aggregator.SvgDispatcher
import org.apache.isis.client.kroviz.to.HasLinks
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.UUID
import org.w3c.files.Blob

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @see "https://en.wikipedia.org/wiki/Proxy_pattern"
 * @see "https://martinfowler.com/eaaDev/EventSourcing.html"
 */
object EventStore {
    var log = observableListOf<LogEntry>()
    var logStartTime: Int = 0

    private fun log(logEntry: LogEntry) {
        log.add(logEntry)
        if (log.size == 1) {
            logStartTime = logEntry.createdAt.getMilliseconds()
        }
    }

    fun start(reSpec: ResourceSpecification,
              method: String,
              body: String = "",
              aggregator: BaseAggregator? = null): LogEntry {
        val entry = LogEntry(reSpec.url, method, request = body, subType = reSpec.subType)
        if (aggregator != null) {
            entry.addAggregator(aggregator)
        }
        log(entry)
        updateStatus(entry)
        return entry
    }

    fun add(reSpec: ResourceSpecification) {
        val entry = LogEntry(url = reSpec.url)
        log(entry)
        updateStatus(entry)
    }

    fun addView(title: String, aggregator: BaseAggregator, panel: SimplePanel) {
        val entry = LogEntry(title = title, aggregator = aggregator)
        entry.panel = panel
        log(entry)
        updateStatus(entry)
    }

    fun closeView(title: String) {
        val logEntry = findView(title)
        if (null != logEntry) {
            logEntry.setClose()
            logEntry.getAggregator().reset()
            updateStatus(logEntry)
        }
    }

    fun end(reSpec: ResourceSpecification, response: String): LogEntry? {
        val entry: LogEntry? = findBy(reSpec)
        if (entry != null) {
            entry.response = response
            entry.setSuccess()
            updateStatus(entry)
        }
        return entry
    }

    fun end(reSpec: ResourceSpecification, pumlCode: String, response: Any?): LogEntry? {
        val entry: LogEntry? = findBy(reSpec, pumlCode)
        if (entry != null) {
            when (response) {
                is String -> entry.response = response
                is Blob -> entry.blob = response
                else -> {
                }
            }
            entry.setSuccess()
            updateStatus(entry)
        }
        return entry
    }

    fun fault(reSpec: ResourceSpecification, fault: String) {
        val entry: LogEntry? = findBy(reSpec)
        entry!!.setError(fault)
        updateStatus(entry)
    }

    internal fun updateStatus(entry: LogEntry) {
        UiManager.updateStatus(entry)
    }

    /**
     * Answers the first matching entry.
     */
    fun findBy(reSpec: ResourceSpecification): LogEntry? {
        return if (reSpec.isRedundant()) {
            findEquivalent(reSpec)
        } else {
            findExact(reSpec)
        }
    }

    fun findBy(reSpec: ResourceSpecification, body: String): LogEntry? {
        return log.firstOrNull() {
            it.url == reSpec.url
                    && it.subType == reSpec.subType
                    && it.request == body
        }
    }

    fun findBy(tObject: TObject): LogEntry? {
        return log.firstOrNull() {
            it.obj is TObject && (it.obj as TObject).instanceId == tObject.instanceId
        }
    }

    fun findBy(aggregator: BaseAggregator): LogEntry? {
        return log.firstOrNull { it.getAggregator() == aggregator }
    }

    fun findAllBy(aggregator: BaseAggregator): List<LogEntry> {
        return log.filter { it.getAggregator() == aggregator }
    }

    fun findByDispatcher(uuid: UUID): LogEntry {
        return log.first {
            val aggt = it.getAggregator()
            aggt is SvgDispatcher
                    && aggt.callBack == uuid
        }
    }

    fun findMenuBars(): LogEntry? {
        return log.firstOrNull() {
            it.obj is Menubars
        }
    }

    //public for test
    fun findExact(reSpec: ResourceSpecification): LogEntry? {
        return log.firstOrNull {
            it.matches(reSpec)
        }
    }

    //public for test
    fun findView(title: String): LogEntry? {
        return log.firstOrNull {
            it.title == title && it.isView()
        }
    }

    //public for test
    fun findEquivalent(reSpec: ResourceSpecification): LogEntry? {
        return log.firstOrNull {
            reSpec.matches(it)
        }
    }

    fun reset() {
        log.removeAll(log)
    }

    fun getLinked(): List<LogEntry> {
        // we use all LE's - eventually to be refined to a single LE (chain up and down)
        val linked = mutableListOf<LogEntry>()
        log.forEach {
            if (it.obj is HasLinks) linked.add(it)
        }
        return linked
    }

}
