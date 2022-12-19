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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.apache.isis.core.metamodel.commons.StringExtensions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodLiteralConstants {
    
    // -- PREFIXES

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";

    public static final String DEFAULT_PREFIX = "default";
    public static final String CHOICES_PREFIX = "choices";
    public static final String AUTO_COMPLETE_PREFIX = "autoComplete";

    public static final String HIDE_PREFIX = "hide";
    public static final String DISABLE_PREFIX = "disable";
    public static final String VALIDATE_PREFIX = "validate";
    
    public static final String CREATED_PREFIX = "created";
    public static final String LOADED_PREFIX = "loaded";
    public static final String LOADING_PREFIX = "loading";
    public static final String SAVED_PREFIX = "saved";
    public static final String SAVING_PREFIX = "saving";
    public static final String PERSISTED_PREFIX = "persisted";
    public static final String PERSISTING_PREFIX = "persisting";
    public static final String DELETED_PREFIX = "deleted";
    public static final String DELETING_PREFIX = "deleting";
    public static final String REMOVED_PREFIX = "removed";
    public static final String REMOVING_PREFIX = "removing";
    public static final String UPDATED_PREFIX = "updated";
    public static final String UPDATING_PREFIX = "updating";
    
    // -- LITERALS
    
    public static final String DISABLED = "disabled"; // for batch disabling all members
    public static final String TITLE = "title";
    public static final String TO_STRING = "toString";
    
    public static final String CSS_CLASS_PREFIX = "cssClass";
    public static final String HIDDEN_PREFIX = "hidden";
    public static final String ICON_NAME_PREFIX = "iconName";
    public static final String LAYOUT_METHOD_NAME = "layout";
    
    // -- SUPPORTING METHOD NAMING CONVENTION
    
    public static enum SupportingMethodNamingConvention {

        /** version 1.x classic eg. hideAct(...), hide0Act(...)*/
        PREFIX_PARAMNUM_ACTION {

            @Override
            protected String getActionSupportingMethodName(Method actionMethod, String prefix) {
                final String capitalizedName = 
                        StringExtensions.asCapitalizedName(actionMethod.getName());
                return prefix + capitalizedName;
            }

            @Override
            protected String getParameterSupportingMethodName(Method actionMethod, String prefix, int paramNum) {
                final String capitalizedName = 
                        StringExtensions.asCapitalizedName(actionMethod.getName());
                return prefix + paramNum + capitalizedName;
            }
            
            @Override
            protected String getMemberSupportingMethodName(Member member, String prefix) {
                if(member instanceof Method) {
                    final Method method = (Method)member;
                    if(method.getParameterCount()>0) {
                        // definitely an action not a getter
                        return getActionSupportingMethodName(method, prefix);
                    }
                    // either a no-arg action or a getter 
                    final String capitalizedName = 
                            StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(member.getName());
                    return prefix + capitalizedName;
                }
                // must be a field then 
                final String capitalizedName = 
                        StringExtensions.asCapitalizedName(member.getName());
                return prefix + capitalizedName;
            }
            
        }
        ;
        
        protected abstract String getActionSupportingMethodName(Method actionMethod, String prefix);
        protected abstract String getParameterSupportingMethodName(Method actionMethod, String prefix, int paramNum);
        /** automatically deals with properties getters and actions */
        protected abstract String getMemberSupportingMethodName(Member member, String prefix);

        /** paramNum to param-supporting-method name provider */
        public IntFunction<String> providerForParam(Method actionMethod, String prefix) {
            return paramNum->getParameterSupportingMethodName(
                    actionMethod, prefix, paramNum);
        }
        
        /** action-supporting-method name provider */
        public Supplier<String> providerForAction(Method actionMethod, String prefix) {
            return ()->getActionSupportingMethodName(actionMethod, prefix);
        }
        
        /** member-supporting-method name provider */
        public Supplier<String> providerForMember(Member member, String prefix) {
            return ()->getMemberSupportingMethodName(member, prefix);
        }
        
    }
     
    
    // -- DEPRECATIONS
    
    @Deprecated public static final String VALIDATE_ADD_TO_PREFIX = "validateAddTo";
    @Deprecated public static final String VALIDATE_REMOVE_FROM_PREFIX = "validateRemoveFrom";
    @Deprecated public static final String CLEAR_PREFIX = "clear";
    @Deprecated public static final String MODIFY_PREFIX = "modify";
    @Deprecated public static final String ADD_TO_PREFIX = "addTo";
    @Deprecated public static final String REMOVE_FROM_PREFIX = "removeFrom";
    

}
