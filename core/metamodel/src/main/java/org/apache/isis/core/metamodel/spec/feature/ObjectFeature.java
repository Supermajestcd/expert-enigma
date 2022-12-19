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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;

/**
 * A specification representing a non-{@link FeatureType#OBJECT object}, that
 * therefore has an underlying type (the type of the property, collection)
 *
 * <p>
 * For a property or action parameter, is the type. For a collection is the
 * element type. For an action it is always <tt>null</tt>.
 */
public interface ObjectFeature extends Specification {

    /**
     * Returns the identifier of the member, which must not change. This should
     * be all camel-case with no spaces: so if the member is called 'Return
     * Date' then a suitable id would be 'returnDate'.
     * @implNote for {@link ObjectMember}(s) this is a shortcut for
     * {@link ObjectMember#getFeatureIdentifier()}.getMemberLogicalName()
     * @see Identifier#getMemberLogicalName()
     */
    String getId();

    /**
     * Returns the (translated friendly) name for this member - the field or action.
     * If not specified, defaults to the java-source-code name of this member.
     *
     * @apiNote argument is a {@link Supplier}, because the {@link ManagedObject}
     * is only required when the name is provided imperatively and domain-object
     * retrieval might be expensive
     *
     * @see #getFeatureIdentifier()
     */
    String getFriendlyName(Supplier<ManagedObject> domainObjectProvider);

    /**
     * Optionally returns the (translated friendly) name for this member -
     * the field or action, based on whether the name is provided statically
     * and not imperatively.
     * <p>
     * If not specified, defaults to the java-source-code name of this member.
     *
     * @see #getFeatureIdentifier()
     */
    Optional<String> getStaticFriendlyName();

    /**
     * Returns a (translated) description of how the member is used - this complements the
     * help text.
     *
     * @apiNote argument is a {@link Supplier}, because the {@link ManagedObject}
     * is only required when the description is provided imperatively and domain-object
     * retrieval might be expensive
     *
     */
    String getDescription(final Supplier<ManagedObject> domainObjectProvider);

    /**
     * Optionally returns the (translated) description of how the member is used,
     * based on whether the description is provided statically
     * and not imperatively.
     */
    Optional<String> getStaticDescription();

    /**
     * The specification of the underlying type.
     *
     * <p>
     * For example:
     * <ul>
     * <li>for a {@link OneToOneAssociation property}, will return the
     * {@link ObjectSpecification} of the type that the accessor returns.
     * <li>for a {@link OneToManyAssociation collection} it will be the type of
     * element the collection holds (not the type of collection).
     * <li>for an {@link ObjectAction action}, will always return <tt>null</tt>.
     * See instead {@link ObjectAction#getReturnType()} and
     * {@link ObjectAction#getParameterTypes()}.
     * <li>for an {@link ObjectActionParameter action parameter}, will return the type of
     * the parameter}.
     * </ul>
     */
    ObjectSpecification getSpecification();

    /**
     * Returns a flag indicating if it can be left unset when the action can be
     * invoked.
     */
    default boolean isOptional() {
        return !MandatoryFacet.isMandatory(this);
    }

}
