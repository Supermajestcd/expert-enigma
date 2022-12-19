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
package org.apache.isis.core.metamodel.facets.all.i8n.noun;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facets.all.i8n.HasMemoizableTranslation;

import lombok.NonNull;

public interface HasNoun
extends HasMemoizableTranslation {

    /**
     * Originating text to be translated before use in the UI.
     * @return {@code Optional.empty()} if {@code nounForm} is not supported
     */
    Optional<String> text(@NonNull NounForm nounForm);

    /**
     * Translated text to be used in the UI.
     * @return {@code Optional.empty()} if {@code nounForm} is not supported
     */
    Optional<String> translated(@NonNull NounForm nounForm);

    ImmutableEnumSet<NounForm> getSupportedNounForms();

    @Override
    default void memoizeTranslations() {
        getSupportedNounForms().forEach(this::translated);
    }

    // -- SHORTCUTS

    /**
     * Originating text of singular noun-form to be translated before use in the UI.
     */
    default @Nullable String singular() {
        return text(NounForm.SINGULAR).orElse(null);
    }

    /**
     * Translated text of singular noun-form to be used in the UI.
     */
    default @Nullable String singularTranslated() {
        return translated(NounForm.SINGULAR).orElse(null);
    }

    /**
     * Originating text of plural noun-form to be translated before use in the UI.
     */
    default @Nullable String plural() {
        return text(NounForm.PLURAL).orElse(null);
    }

    /**
     * Translated text of plural noun-form to be used in the UI.
     */
    default @Nullable String pluralTranslated() {
        return translated(NounForm.PLURAL).orElse(null);
    }


}
