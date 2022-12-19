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
package org.apache.isis.metamodel.adapter.oid;

import static org.apache.isis.commons.internal.base._Strings.splitThenStream;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.assertions._Ensure;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.spec.ObjectSpecId;

/**
 * Factory for subtypes of {@link Oid}, based on their oid str.
 *
 * <p>
 * Examples
 * <dl>
 * <dt>CUS:123</dt>
 * <dd>persistent root</dd>
 * <dt>!CUS:123</dt>
 * <dd>transient root</dd>
 * <dt>*CUS:123</dt>
 * <dd>view model root</dd>
 * <dt>CUS:123$items</dt>
 * <dd>collection of persistent root</dd>
 * <dt>!CUS:123$items</dt>
 * <dd>collection of transient root</dd>
 * <dt>CUS:123~NME:2</dt>
 * <dd>aggregated object within persistent root</dd>
 * <dt>!CUS:123~NME:2</dt>
 * <dd>aggregated object within transient root</dd>
 * <dt>CUS:123~NME:2~CTY:LON</dt>
 * <dd>aggregated object within aggregated object within root</dd>
 * <dt>CUS:123~NME:2$items</dt>
 * <dd>collection of an aggregated object within root</dd>
 * <dt>CUS:123~NME:2~CTY:LON$streets</dt>
 * <dd>collection of an aggregated object within aggregated object within root</dd>
 * </dl>
 *
 * <p>
 * Separators:
 * <dl>
 * <dt>!</dt>
 * <dd>precedes root object type, indicates transient</dd>
 * <dt>*</dt>
 * <dd>precedes root object type, indicates transient</dd>
 * <dt>:</dt>
 * <dd>precedes root object identifier</dd>
 * <dt>~</dt>
 * <dd>precedes aggregate oid</dd>
 * <dt>$</dt>
 * <dd>precedes collection name</dd>
 * <dt>^</dt>
 * <dd>precedes version</dd>
 * </dl>
 *
 * <p>
 * Note that # and ; were not chosen as separators to minimize noise when URL encoding OIDs.
 */
final class Oid_Marshaller implements Oid.Marshaller, Oid.Unmarshaller {

    public final static Oid_Marshaller INSTANCE = new Oid_Marshaller();

    private Oid_Marshaller(){}

    // -- public constants
    public static final String VIEWMODEL_INDICATOR =
            Bookmark.ObjectState.VIEW_MODEL.getCode(); // "*"


    // -- private constants
    private static final String TRANSIENT_INDICATOR =
            Bookmark.ObjectState.TRANSIENT.getCode() ; // "!"

    private static final String SEPARATOR = ":";
    private static final String SEPARATOR_NESTING = "~";
    private static final String SEPARATOR_PARENTED = "$";
    private static final String SEPARATOR_VERSION = "^";

    private static final String WORD = "[^" + SEPARATOR + SEPARATOR_NESTING + SEPARATOR_PARENTED + "\\" + SEPARATOR_VERSION + "#" + "]+";
    private static final String DIGITS = "\\d+";

    private static final String WORD_GROUP = "(" + WORD + ")";
    private static final String DIGITS_GROUP = "(" + DIGITS + ")";

    private static Pattern OIDSTR_PATTERN =
            Pattern.compile(
                    "^(" +
                            "(" +
                            "([" + TRANSIENT_INDICATOR + VIEWMODEL_INDICATOR + "])?" +
                            WORD_GROUP + SEPARATOR + WORD_GROUP +
                            ")" +
                            "(" +
                            "(" + SEPARATOR_NESTING + WORD + SEPARATOR + WORD + ")*" + // nesting of aggregates
                            ")" +
                            ")" +
                            "(" + "[" + SEPARATOR_PARENTED + "]" + WORD + ")?"  + // optional collection name
                            "(" +
                            "[\\" + SEPARATOR_VERSION + "]" +
                            DIGITS_GROUP +                    // optional version digit
                            SEPARATOR + WORD_GROUP + "?" +    // optional version user name
                            SEPARATOR + DIGITS_GROUP + "?" +  // optional version UTC time
                            ")?" +
                    "$");


    // -- join, split
    
    @Override //implementing Oid.Marshaller
    public String joinAsOid(String domainType, String instanceId) {
        return domainType + SEPARATOR + instanceId;
    }

    @Override //implementing Oid.Unarshaller
    public String splitInstanceId(String oidStr) {
        final int indexOfSeperator = oidStr.indexOf(SEPARATOR);
        return indexOfSeperator > 0? oidStr.substring(indexOfSeperator+1): null;
    }



    // -- unmarshal

    @Override
    public <T extends Oid> T unmarshal(String oidStr, Class<T> requestedType) {

        final Matcher matcher = OIDSTR_PATTERN.matcher(oidStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse OID '" + oidStr + "'; should match pattern: " + OIDSTR_PATTERN.pattern());
        }

        final String isTransientOrViewModelStr = getGroup(matcher, 3);
        final Oid_State state;
        if("!".equals(isTransientOrViewModelStr)) {
            state = Oid_State.TRANSIENT;
        } else if("*".equals(isTransientOrViewModelStr)) {
            state = Oid_State.VIEWMODEL;
        } else {
            state = Oid_State.PERSISTENT;
        }

        final String rootObjectType = getGroup(matcher, 4);
        final String rootIdentifier = getGroup(matcher, 5);

        final String aggregateOidPart = getGroup(matcher, 6);
        final List<AggregateOidPart> aggregateOidParts = _Lists.newArrayList();
//        final Splitter nestingSplitter = Splitter.on(SEPARATOR_NESTING);
//        final Splitter partsSplitter = Splitter.on(SEPARATOR);
        if(aggregateOidPart != null) {
            final Stream<String> tildaSplitted = splitThenStream(aggregateOidPart, SEPARATOR_NESTING); 
                    
            tildaSplitted.forEach(str->{
                if(_Strings.isNullOrEmpty(str)) {
                    return; // leading "~"
                }
                final Iterator<String> colonSplitIter = splitThenStream(str, SEPARATOR).iterator();
                final String objectType = colonSplitIter.next();
                final String localId = colonSplitIter.next();
                aggregateOidParts.add(new AggregateOidPart(objectType, localId));
            });
            
        }
        final String collectionPart = getGroup(matcher, 8);
        final String collectionName = collectionPart != null ? collectionPart.substring(1) : null;

        final String versionSequence = getGroup(matcher, 10);
        final String versionUser = getGroup(matcher, 11);
        final String versionUtcTimestamp = getGroup(matcher, 12);
        final Version version = Version.Factory.parse(versionSequence, versionUser, versionUtcTimestamp);

        if(collectionName == null) {
            if(aggregateOidParts.isEmpty()) {
                ensureCorrectType(oidStr, requestedType, RootOid.class);
                return _Casts.uncheckedCast(
                        Oid_Root.of(ObjectSpecId.of(rootObjectType), rootIdentifier, state, version));
            } else {
                throw new RuntimeException("Aggregated Oids are no longer supported");
            }
        } else {
            final String oidStrWithoutCollectionName = getGroup(matcher, 1);

            final String parentOidStr = oidStrWithoutCollectionName + marshal(version);

            RootOid parentOid = this.unmarshal(parentOidStr, RootOid.class);
            ensureCorrectType(oidStr, requestedType, ParentedOid.class);
            return _Casts.uncheckedCast( Oid_Parented.ofName(parentOid, collectionName) );
        }
    }



    private static class AggregateOidPart {
        AggregateOidPart(String objectType, String localId) {
            this.objectType = objectType;
            this.localId = localId;
        }
        String objectType;
        String localId;
        @Override
        public String toString() {
            return SEPARATOR_NESTING + objectType + SEPARATOR + localId;
        }
    }


    private <T> void ensureCorrectType(String oidStr, Class<T> requestedType, 
            final Class<? extends Oid> actualType) {
        
        if(!requestedType.isAssignableFrom(actualType)) {
            throw new IllegalArgumentException(
                    String.format("OID '%s' was unmarshealled to type '%s' which cannot be assigned "
                            + "to requested type '%s'",
                            oidStr, actualType.getSimpleName(), requestedType.getSimpleName()) );
        }
    }

    private String getGroup(final Matcher matcher, final int group) {
        final int groupCount = matcher.groupCount();
        if(group > groupCount) {
            return null;
        }
        final String val = matcher.group(group);
        return _Strings.emptyToNull(val);
    }


    // -- marshal
    @Override
    public final String marshal(RootOid rootOid) {
        _Ensure.ensure("can not marshal values", !rootOid.isValue());
        return marshalNoVersion(rootOid) + marshal(rootOid.getVersion());
    }
    
    @Override
    public final String marshalNoVersion(RootOid rootOid) {
        _Ensure.ensure("can not marshal values", !rootOid.isValue());
        final String transientIndicator = rootOid.isTransient()? TRANSIENT_INDICATOR : "";
        final String viewModelIndicator = rootOid.isViewModel()? VIEWMODEL_INDICATOR : "";
        return transientIndicator + viewModelIndicator + rootOid.getObjectSpecId() + SEPARATOR + rootOid.getIdentifier();
    }

    @Override
    public final String marshal(ParentedOid parentedOid) {
        return marshalNoVersion(parentedOid) + marshal(parentedOid.getVersion());
    }

    @Override
    public String marshalNoVersion(ParentedOid parentedOid) {
        return parentedOid.getParentOid().enStringNoVersion() + SEPARATOR_PARENTED + parentedOid.getName();
    }

    @Override
    public final String marshal(Version version) {
        if(Version.isEmpty(version)) {
            return "";
        }
        final String versionUser = version.getUser();
        return SEPARATOR_VERSION + version.getSequence() + SEPARATOR 
                + _Strings.nullToEmpty(versionUser) + SEPARATOR + 
                (version.hasTimestamp() ? version.getUtcTimestamp() : "");
    }



}
