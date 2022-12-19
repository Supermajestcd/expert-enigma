package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.model.DisplayModelWithLayout
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.Represention
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.kv.Constants

abstract class AggregatorWithLayout : BaseAggregator() {

    protected fun handleLayout(layout: Layout, dm: DisplayModelWithLayout) {
        if (dm.layout == null) {
            dm.addLayout(layout)
            dm.propertyLayoutList.forEach { p ->
                val l = p.link!!
                val isDn = l.href.contains("datanucleus")
                if (isDn) {
                    //invoking DN links leads to an error
                    invoke(l, this)
                }
            }
        }
    }

    protected fun invokeLayoutLink(obj: TObject) {
        val l = obj.getLayoutLink()!!
        if (l.representation() == Represention.OBJECT_LAYOUT_BS3) {
            invoke(l, this, Constants.subTypeXml)
        } else {
            invoke(l, this)
        }
    }

}
