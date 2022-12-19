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
package org.apache.isis.testdomain.model.good;

import java.util.List;
import java.util.Set;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.Setter;


/**
 *
 * <h1>Support Matrix</h1>
 * <pre>
 * Prefix     Obj Prop Coll Act Param
 * autoComplete    x             x
 * choices         x             x
 * default         x             x
 * disable     x   x    x    x
 * described       x    x    x
 * hide            x    x    x
 * named           x    x    x
 * validate        x         x   x
 * </pre>
 *
 */
@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperMemberSupport {

    // -- PROPER ACTION

    @Action
    public void myAction(final String p0, final String p1) {

    }

    @MemberSupport
    public String namedMyAction() {
        return null;
    }

    @MemberSupport
    public String describedMyAction() {
        return null;
    }

    @MemberSupport
    public String disableMyAction() {
        return null;
    }

    @MemberSupport // variant with dependent args
    public String disable1MyAction(final String p0) {
        return null;
    }

    @MemberSupport
    public boolean hideMyAction() {
        return false;
    }

    @MemberSupport // variant with dependent args
    public boolean hide1MyAction(final String p0) {
        return false;
    }

    @MemberSupport
    public String validateMyAction(final String p0, final String p1) {
        return null;
    }

    @MemberSupport
    public Set<String> autoComplete0MyAction(@MinLength(3) final String search) {
        return null;
    }

    @MemberSupport
    public Set<String> autoComplete1MyAction(@MinLength(3) final String search) {
        return null;
    }

    @MemberSupport
    public Set<String> choices0MyAction() {
        return null;
    }

    @MemberSupport
    public Set<String> choices1MyAction() {
        return null;
    }

// variant with dependent args
//    @MemberSupport
//    public Set<String> choices1MyAction(String p0) {
//        return null;
//    }

    @MemberSupport
    public String default0MyAction() {
        return null;
    }

    @MemberSupport
    public String default1MyAction() {
        return null;
    }

    @MemberSupport
    public String validate0MyAction(final String p0) {
        return null;
    }

    @MemberSupport
    public String validate1MyAction(final String p1) {
        return null;
    }

    // [ISIS-2561] should be detected as Action (despite the prefix, which normally suggests otherwise)
    @Action
    public String hideMe() {
        return null;
    }

    // -- PROPER PROPERTY

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    @Getter @Setter private String myProp;

    @MemberSupport
    public String namedMyProp() {
        return null;
    }

    @MemberSupport
    public String describedMyProp() {
        return null;
    }

    @MemberSupport
    public Set<String> autoCompleteMyProp(@MinLength(3) final String search) {
        return null;
    }

    @MemberSupport
    public Set<String> choicesMyProp() {
        return null;
    }

    @MemberSupport
    public String defaultMyProp() {
        return "";
    }

    @MemberSupport
    public String disableMyProp() {
        return null;
    }

    @MemberSupport
    public boolean hideMyProp() {
        return false;
    }

    @MemberSupport
    public String validateMyProp(final String x) {
        return "think twice";
    }

    // -- PROPER COLLECTION

    @Collection
    @CollectionLayout(named = "foo", describedAs = "bar")
    @Getter @Setter private List<String> myColl;

    @MemberSupport
    public String namedMyColl() {
        return null;
    }

    @MemberSupport
    public String describedMyColl() {
        return null;
    }


    @MemberSupport
    public String disableMyColl() {
        return null;
    }

    @MemberSupport
    public boolean hideMyColl() {
        return false;
    }

    // -- PROPER ACTION - NEW PARAMETER MODEL - XXX only supported with mixins yet
/*
    @Value @Accessors(fluent = true)
    public static class Parameter {
        List<String> a;
        List<String> b;
    }

    public ProperMemberSupport act(List<String> a, List<String> b) {
        return this;
    }

    @MemberSupport
    public String disableAct() {
        return null;
    }

    @MemberSupport
    public boolean hideAct() {
        return false;
    }

    @MemberSupport
    public String validateAct(Parameter params) {
        return null;
    }

    @MemberSupport
    public Set<String> autoComplete0Act(Parameter params, @MinLength(3) String search) {
        return null;
    }

    @MemberSupport
    public Set<String> autoComplete1Act(Parameter params, @MinLength(3) String search) {
        return null;
    }

    @MemberSupport
    public Set<String> choices0Act(Parameter params) {
        return null;
    }

    @MemberSupport
    public Set<String> choices1Act(Parameter params) {
        return null;
    }

    @MemberSupport
    public List<String> default0Act(Parameter params) {
        return null;
    }

    @MemberSupport
    public List<String> default1Act(Parameter params) {
        return null;
    }

    @MemberSupport
    public String validate0Act(Parameter params) {
        return null;
    }

    @MemberSupport
    public String validate1Act(Parameter params) {
        return null;
    }
*/
}
