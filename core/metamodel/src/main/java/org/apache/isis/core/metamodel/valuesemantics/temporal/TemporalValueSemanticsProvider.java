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
package org.apache.isis.core.metamodel.valuesemantics.temporal;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.TimePrecision;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.semantics.TemporalValueSemantics;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.objectvalue.temporalformat.DateFormatStyleFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.temporalformat.TimeFormatPrecisionFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.temporalformat.TimeFormatStyleFacet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * Common base for {@link java.time.temporal.Temporal} types.
 *
 * @since 2.0
 *
 * @param <T> implementing {@link java.time.temporal.Temporal} type
 */
//@Log4j2
public abstract class TemporalValueSemanticsProvider<T extends Temporal>
extends ValueSemanticsAbstract<T>
implements TemporalValueSemantics<T> {

    @Inject protected MetaModelContext mmc;

    @Getter(onMethod_ = {@Override}) protected final TemporalCharacteristic temporalCharacteristic;
    @Getter(onMethod_ = {@Override}) protected final OffsetCharacteristic offsetCharacteristic;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) protected final int typicalLength;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) protected final int maxLength;

    /**
     * Keys represent the values which can be configured,
     * and which are used for the rendering of dates.
     */
    protected final TemporalQuery<T> query;
    protected final BiFunction<TemporalAdjust, T, T> adjuster;

    protected TemporalValueSemanticsProvider(
            final TemporalCharacteristic temporalCharacteristic,
            final OffsetCharacteristic offsetCharacteristic,
            final int typicalLength,
            final int maxLength,
            final TemporalQuery<T> query,
            final BiFunction<TemporalAdjust, T, T> adjuster) {

        super();

        this.temporalCharacteristic = temporalCharacteristic;
        this.offsetCharacteristic = offsetCharacteristic;
        this.typicalLength = typicalLength;
        this.maxLength = maxLength;

        this.query = query;
        this.adjuster = adjuster;
    }

    // -- ORDER RELATION

    protected final static Duration ALMOST_A_SECOND = Duration.ofNanos(999_999_999);
    protected final static Duration ALMOST_A_MILLI_SECOND = Duration.ofNanos(999_999);

    @Override
    public final int compare(final T a, final T b, final @NonNull Duration epsilon) {

        val delta = (!a.isSupported(ChronoUnit.SECONDS))
                ? Duration.ofDays(a.until(b, ChronoUnit.DAYS))
                : Duration.between(a, b);

        if(epsilon.minus(delta.abs()).isNegative()) {
            // negative delta means a > b => should return +1
            return delta.isNegative()
                    ? 1
                    : -1;
        }
        return 0;
    }

    @Override
    public final boolean equals(final T a, final T b, final @NonNull Duration epsilon) {
        return compare(a, b, epsilon) == 0;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final T temporal) {
        return decomposeAsNullable(temporal, UnaryOperator.identity(), ()->null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, dto->(T)CommonDtoUtils.getValueAsObject(dto), UnaryOperator.identity(), ()->null);
    }

    // -- RENDERER

    @Override
    public final String simpleTextPresentation(
            final ValueSemanticsProvider.Context context,
            final T value) {
        return render(value, getRenderingFormat(context)::format);
    }

    // -- PARSER

    @Override
    public final String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final T value) {
        return value==null ? "" : getEditingOutputFormat(context).format(value);
    }

    @Override
    public final T parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val temporalString = _Strings.blankToNullOrTrim(text);
        if(temporalString==null) {
            return null;
        }

        T contextTemporal = null; //FIXME[ISIS-2882] not implemented yet
        if(contextTemporal != null) {
            val adjusted = TemporalAdjust
                    .parseAdjustment(adjuster, contextTemporal, temporalString);
            if(adjusted!=null) {
                return adjusted;
            }
        }

        val format = getEditingInputFormat(context);

        try {
            return format.parse(temporalString, query);
        } catch (final Exception e) {
            throw new TextEntryParseException(String.format("Not recognised as a %s: %s",
                    getCorrespondingClass().getName(),
                    temporalString), e);
        }

    }

    /**
     * Format for pretty rendering, not used for parsing/editing.
     */
    protected DateTimeFormatter getRenderingFormat(final ValueSemanticsProvider.Context context) {

        val dateAndTimeFormatStyle = DateAndTimeFormatStyle.forContext(mmc, context);

        return getTemporalRenderingFormat(
                context, temporalCharacteristic, offsetCharacteristic,
                dateAndTimeFormatStyle.getDateFormatStyle(),
                dateAndTimeFormatStyle.getTimeFormatStyle());
    }

    /**
     * Format used for rendering editable text representation.
     */
    protected DateTimeFormatter getEditingOutputFormat(final ValueSemanticsProvider.Context context) {

        val dateAndTimeFormatStyle = DateAndTimeFormatStyle.forContext(mmc, context);

        return getTemporalEditingFormat(context, temporalCharacteristic, offsetCharacteristic,
                dateAndTimeFormatStyle.getTimePrecision(),
                EditingFormatDirection.OUTPUT,
                temporalEditingPattern());
    }

    /**
     * Format used for parsing editable text representation.
     */
    protected DateTimeFormatter getEditingInputFormat(final ValueSemanticsProvider.Context context) {

        val dateAndTimeFormatStyle = DateAndTimeFormatStyle.forContext(mmc, context);

        return getTemporalEditingFormat(context, temporalCharacteristic, offsetCharacteristic,
                dateAndTimeFormatStyle.getTimePrecision(),
                EditingFormatDirection.INPUT,
                temporalEditingPattern());
    }

    @Override
    public String getPattern(final ValueSemanticsProvider.Context context) {

        val dateAndTimeFormatStyle = DateAndTimeFormatStyle.forContext(mmc, context);

        return temporalEditingPattern()
                .getEditingFormatAsPattern(temporalCharacteristic, offsetCharacteristic,
                        dateAndTimeFormatStyle.getTimePrecision(),
                        EditingFormatDirection.OUTPUT);
    }

    /**
     * ISO format used for serializing.
     */
    protected DateTimeFormatter getIsoFormat() {
        return getTemporalIsoFormat(temporalCharacteristic, offsetCharacteristic);
    }

    // -- HELPER

    @Value(staticConstructor = "of")
    static class DateAndTimeFormatStyle {
        @Nullable FormatStyle dateFormatStyle;
        @Nullable FormatStyle timeFormatStyle;
        @Nullable TimePrecision timePrecision;

        static DateAndTimeFormatStyle forContext(
                final @Nullable MetaModelContext mmc, // nullable .. JUnit support
                final @Nullable ValueSemanticsProvider.Context context) {

            val featureIfAny = Optional.ofNullable(mmc)
                    .map(MetaModelContext::getSpecificationLoader)
                    .flatMap(specLoader->specLoader.loadFeature(
                            Optional.ofNullable(context)
                            .map(ValueSemanticsProvider.Context::getFeatureIdentifier)
                            .orElse(null)));

            val dateFormatStyle = featureIfAny
                    .flatMap(feature->feature.lookupFacet(DateFormatStyleFacet.class))
                    .map(DateFormatStyleFacet::getDateFormatStyle)
                    .orElse(FormatStyle.MEDIUM);

            val timeFormatStyle = featureIfAny
                    .flatMap(feature->feature.lookupFacet(TimeFormatStyleFacet.class))
                    .map(TimeFormatStyleFacet::getTimeFormatStyle)
                    .orElse(FormatStyle.MEDIUM);

            val timePrecision = featureIfAny
                    .flatMap(feature->feature.lookupFacet(TimeFormatPrecisionFacet.class))
                    .map(TimeFormatPrecisionFacet::getTimePrecision)
                    .orElse(TimePrecision.SECOND);

            return of(dateFormatStyle, timeFormatStyle, timePrecision);
        }

    }

    private org.apache.isis.core.config.IsisConfiguration.ValueTypes.Temporal temporalConfig() {
        return Optional.ofNullable(mmc) // nullable .. JUnit support
                .map(MetaModelContext::getConfiguration)
                .map(conf->conf.getValueTypes().getTemporal())
                .orElseGet(IsisConfiguration.ValueTypes.Temporal::new);
    }

    private TemporalEditingPattern temporalEditingPattern() {
        return temporalConfig().getEditing();
    }

}
