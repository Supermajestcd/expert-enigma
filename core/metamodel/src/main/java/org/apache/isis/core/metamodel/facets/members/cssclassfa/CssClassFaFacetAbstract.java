/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.core.metamodel.facets.members.cssclassfa;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.functions._Predicates;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.val;

public class CssClassFaFacetAbstract extends FacetAbstract implements CssClassFaFacet {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final String FIXED_WIDTH = "fa-fw";
    private static final String DEFAULT_PRIMARY_PREFIX = "fa";
    
    
    @Getter(onMethod = @__(@Override)) private CssClassFaPosition position;
    private List<String> cssClasses; // serializable list implementation
    
    public CssClassFaFacetAbstract(
            final String value, 
            final CssClassFaPosition position,
            final FacetHolder holder) {
        
        super(CssClassFaFacet.class, holder);
        this.position = position;
        this.cssClasses = parse(value);
    }

    @Override
    public Stream<String> streamCssClasses() {
        return _NullSafe.stream(cssClasses);
    }
    
    @Override 
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("position", position);
        attributeMap.put("classes", asSpaceSeparated());
    }
    
    // -- HELPER
    
    /**
     * Parses given value for CSS classes (space separated).
     * <ul>
     * <li> 
     * Adds the optional <em>fa-fw</em> fixed width FontAwesome class, if not provided.
     * <li>
     * Adds the default <em>fa</em> FontAwesome prefix class, if no other prefix class provided (fab, far or fas).
     * </ul>
     * @param parsedClasses
     * @return The original CSS classes plus <em>fa</em> and <em>fa-fw</em> if not already provided
     */
    static List<String> parse(String value) {
        val cssClassesSet = _Sets.<String>newLinkedHashSet(); // preserved order
        _Strings.splitThenStreamTrimmed(value.trim(), WHITESPACE)
        .map(CssClassFaFacetAbstract::faPrefix)
        .forEach(cssClass->cssClassesSet.add(faPrefix(cssClass)));
        
        return sanitize(cssClassesSet);
    }

    private static List<String> sanitize(final Set<String> parsedClasses) {
        val cssClasses = _Lists.<String>newArrayList();
        
        val primaryPrefix = parsedClasses.stream()
        .filter(CssClassFaFacetAbstract::isFaPrimaryPrefix)
        .findFirst()
        .orElse(DEFAULT_PRIMARY_PREFIX);
        
        cssClasses.add(primaryPrefix);
        cssClasses.add(FIXED_WIDTH);
        
        parsedClasses.stream()
        .filter(_Predicates.not(CssClassFaFacetAbstract::isFaPrimaryPrefix))
        .filter(_Predicates.not(CssClassFaFacetAbstract::isFixedWidth))
        .forEach(cssClasses::add);
        
        return cssClasses;
    }

    private static String faPrefix(final String cssClass) {
        return cssClass.startsWith("fa-") || isFaPrimaryPrefix(cssClass)
                ? cssClass
                        : "fa-" + cssClass;
    }
    
    private static boolean isFixedWidth(final String cssClass) {
        return FIXED_WIDTH.equals(cssClass);
    }

    private static boolean isFaPrimaryPrefix(final String cssClass) {
        if(_NullSafe.isEmpty(cssClass)) {
            return false;
        }
        switch(cssClass) {
        case "fa":
        case "far":
        case "fab":
        case "fas":
            return true;
        default:
            return false;
        }
        
    }


    
}
