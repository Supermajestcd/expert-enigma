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
package org.apache.causeway.testdomain.model.bad;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.testdomain.model.base.MemberDetection;

public class OrphanedMemberSupportDetection {


    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_OPTIONAL)
    public static class WhenAnnotationOptional
    extends MemberDetection.PublicBase {

        // expected to produce orphans for ...

        // void placeOrder(final String x, final String y) {}
        // @Getter @Setter String email;
        // @Getter @Setter java.util.Collection<String> orders;
    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_REQUIRED)
    public static class WhenAnnotationRequired
    extends MemberDetection.PublicBase {

        // expected to produce orphans for ...

        // void placeOrder(final String x, final String y) {}
        // @Getter @Setter String email;
        // @Getter @Setter java.util.Collection<String> orders;
    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ENCAPSULATION_ENABLED)
    public static class WhenEncapsulationEnabled
    extends MemberDetection.ProtectedBase {

        // expected to produce orphans for ...

        // void placeOrder(final String x, final String y) {}
        // @Getter @Setter String email;
        // @Getter @Setter java.util.Collection<String> orders;
    }

}
