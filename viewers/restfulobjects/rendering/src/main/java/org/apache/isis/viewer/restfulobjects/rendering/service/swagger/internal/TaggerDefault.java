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
package org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Programmatic;

@Component
@Named("isisMetaModel.TaggerDefault")
public class TaggerDefault implements Tagger {

    static Pattern tagSpringFramework = Pattern.compile("^org\\.springframework\\.([^\\.]+)\\.(.+)$");
    static Pattern tagPatternIsisExtensions2 = Pattern.compile("^isisExt(.+)$");
    static Pattern tagPatternIsisExtensions = Pattern.compile("^org\\.apache\\.isis\\.extensions\\.([^\\.]+)\\.(.+)$");
    static Pattern tagPatternForFqcn = Pattern.compile("^.*\\.([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForTwoParts = Pattern.compile("^([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForJaxbDto = Pattern.compile("^.*\\.([^\\.]+)\\.(v[0-9][^\\.]*)\\.([^\\.]+)$");

    @Override
    @Programmatic
    public String tagForObjectType(final String objType, final String fallback) {

        if (objType.startsWith("org.apache.isis.")) {
            return ". apache isis internals";
        }
        
        Matcher matcher;
        matcher = tagSpringFramework.matcher(objType);
        if (matcher.matches()) {
            return "> spring framework " + matcher.group(1);
        }
        matcher = tagPatternIsisExtensions.matcher(objType);
        if (matcher.matches()) {
            return ". apache isis extensions - " + matcher.group(1);
        }
        matcher = tagPatternForJaxbDto.matcher(objType);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        matcher = tagPatternForFqcn.matcher(objType);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        matcher = tagPatternForTwoParts.matcher(objType);
        if (matcher.matches()) {
            if (objType.startsWith("isisApplib")) {
                return ". apache isis applib";
            }

            if (objType.startsWith("isisExt")) {
                return ". apache isis extensions - " + matcher.group(1);
            }

            return matcher.group(1);
        }
        
        return fallback != null? fallback: objType;
    }

}
