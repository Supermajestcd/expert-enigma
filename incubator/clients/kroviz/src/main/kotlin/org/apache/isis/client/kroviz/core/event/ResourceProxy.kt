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

import org.apache.isis.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.core.aggregator.SvgDispatcher
import org.apache.isis.client.kroviz.handler.ResponseHandler
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants

/**
 * Facade for RoXmlHttpRequest. If a resource is being fetched, it:
 *
 * * looks in EventStore, if a (similar) request has been issued before and can be retrieved from local storage, and if
 * 1. No: issue the real request and handle the response via ResponseHandler (chain)
 * 2. Yes: use the cached response and pass it directly to the respective Aggregator/Dispatcher
 *
 */
class ResourceProxy {

    // there may be more than one aggt - which may break this code
    // we are coming from a parented collection ...
    // we can assume the object hat been loaded as part of the collection before
    fun load(tObject: TObject) {
        val aggregator = ObjectAggregator(tObject.title)
        // ASSUMPTION: there can be max one LogEntry for an Object
        val le = EventStore.findBy(tObject)
        if (le != null) {
            le.addAggregator(aggregator)
            aggregator.update(le, le.subType)
        }
    }

    private fun processCached(rs: ResourceSpecification, aggregator: BaseAggregator?) {
        val le = EventStore.findBy(rs)!!
        le.retrieveResponse()
        if (aggregator == null) {
            ResponseHandler.handle(le)
        } else {
            aggregator.update(le, le.subType)
        }
        le.setCached()
        EventStore.updateStatus(le)
    }

    fun fetch(link: Link,
              aggregator: BaseAggregator? = null,
              subType: String = Constants.subTypeJson,
              isRest: Boolean = true) {
        val rs = ResourceSpecification(link.href)
        val isCached = when (val le = EventStore.findBy(rs)) {
            null -> false
            else -> le.isCached(rs, link.method)
        }
        when {
            isCached -> processCached(rs, aggregator)
            !isCached && isRest -> RoXmlHttpRequest(aggregator).process(link, subType)
            !isCached && !isRest -> RoXmlHttpRequest(aggregator).processNonREST(link, subType)
        }
    }

    private fun isNotRenderedYet(aggregator: BaseAggregator?): Boolean {
        if (aggregator != null && aggregator is AggregatorWithLayout) {
            return !aggregator.dpm.isRendered
        } else {
            return false
        }
    }

    fun invokeKroki(pumlCode: String, aggregator: SvgDispatcher) {
        RoXmlHttpRequest(aggregator).invokeKroki(pumlCode)
    }

}
