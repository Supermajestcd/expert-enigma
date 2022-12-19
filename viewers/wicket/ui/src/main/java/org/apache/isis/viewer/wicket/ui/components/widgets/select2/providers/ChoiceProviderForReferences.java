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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.commons.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

public class ChoiceProviderForReferences
extends ChoiceProviderAbstactForScalarModel {

    private static final long serialVersionUID = 1L;

    static enum Mode {
        CHOICES,
        AUTO_COMPLETE,
        FALLBACK;
        static Mode valueOf(final ScalarModel scalarModel) {
            if (scalarModel.hasChoices()) {
                return Mode.CHOICES;
            } else if(scalarModel.hasAutoComplete()) {
                return Mode.AUTO_COMPLETE;
            } else {
                return Mode.FALLBACK;
            }
        }
    }

    private final Mode mode;

    public ChoiceProviderForReferences(
            final ScalarModel scalarModel) {
        super(scalarModel);
        this.mode = Mode.valueOf(scalarModel);
    }

    @Override
    protected Can<ObjectMemento> query(final String term) {
        switch(mode) {
        case CHOICES:
            return super.filter(term, queryAll());
        case AUTO_COMPLETE:
            return queryWithAutoComplete(term);
        case FALLBACK:
            // fall through
        }
        val scalarTypeSpec = scalarModel().getScalarTypeSpec();
        val autoCompleteAdapters = Facets.autoCompleteExecute(scalarTypeSpec, term);
        return autoCompleteAdapters.map(getCommonContext()::mementoFor);
    }

    // -- HELPER

    private Can<ObjectMemento> queryAll() {
        return scalarModel().getChoices() // must not return detached entities
                .map(getCommonContext()::mementoForParameter);
    }

    private Can<ObjectMemento> queryWithAutoComplete(final String term) {
        val commonContext = getCommonContext();
        val scalarModel = scalarModel();
        val pendingArgs = scalarModel.isParameter()
                ? ((ParameterUiModel)scalarModel).getParameterNegotiationModel().getParamValues()
                : Can.<ManagedObject>empty();
        val pendingArgMementos = pendingArgs
                .map(commonContext::mementoForParameter);

        if(scalarModel.isParameter()) {
            // recover any pendingArgs
            val paramModel = (ParameterUiModel)scalarModel;

            paramModel
                .getParameterNegotiationModel()
                .setParamValues(
                        reconstructPendingArgs(paramModel, pendingArgMementos));
        }

        return scalarModel
                .getAutoComplete(term)
                .map(commonContext::mementoFor);
    }

    private Can<ManagedObject> reconstructPendingArgs(
            final ParameterUiModel parameterModel,
            final Can<ObjectMemento> pendingArgMementos) {

        val commonContext = super.getCommonContext();
        val pendingArgsList = _NullSafe.stream(pendingArgMementos)
            .map(commonContext::reconstructObject)
            .map(ManagedObject.class::cast)
            .collect(Can.toCan());

       return pendingArgsList;
    }


}
