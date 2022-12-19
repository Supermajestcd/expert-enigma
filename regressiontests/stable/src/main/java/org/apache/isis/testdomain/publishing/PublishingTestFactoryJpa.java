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
package org.apache.isis.testdomain.publishing;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Functions.CheckedConsumer;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.persistence.jpa.applib.services.JpaSupportService;
import org.apache.isis.testdomain.jpa.JpaTestDomainPersona;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testdomain.jpa.entities.JpaProduct;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.CommitListener;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Component
@Import({
    CommitListener.class
})
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PublishingTestFactoryJpa
extends PublishingTestFactoryAbstract {

    private final RepositoryService repository;
    private final WrapperFactory wrapper;
    private final ObjectManager objectManager;
    private final FixtureScripts fixtureScripts;
    private final CommitListener commitListener;
    private final JpaSupportService jpaSupport;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final InteractionService interactionService;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final TransactionService transactionService;

    // -- TEST SETUP

    @Override
    protected void setupEntity(final PublishingTestContext context) {
        // given
        setupBookForJpa();
    }

    // -- TESTS - PROGRAMMATIC

    @Override
    protected boolean programmaticExecution(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // This test does not trigger command or execution publishing, however it does trigger
        // entity-change-publishing.

        withBookDo(book->{

            context.runGiven();

            // when - direct change (circumventing the framework)
            context.changeProperty(()->book.setName("Book #2"));

            repository.persistAndFlush(book);

        });


        return true;
    }

    // -- TESTS - INTERACTION API

    @Override
    protected boolean interactionApiExecution(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // when
        withBookDo(book->{

            context.runGiven();

            // when
            context.changeProperty(()->{

                val bookAdapter = objectManager.adapt(book);
                val propertyInteraction = PropertyInteraction.start(bookAdapter, "name", Where.OBJECT_FORMS);
                val managedProperty = propertyInteraction.getManagedPropertyElseThrow(__->_Exceptions.noSuchElement());
                val propertyModel = managedProperty.startNegotiation();
                val propertySpec = managedProperty.getSpecification();
                propertyModel.getValue().setValue(ManagedObject.of(propertySpec, "Book #2"));
                propertyModel.submit();

            });

        });

        return true;
    }

    // -- TESTS - WRAPPER SYNC

    @Override
    protected boolean wrapperSyncExecutionNoRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // when
        withBookDo(book->{

            context.runGiven();

            // when - running synchronous
            val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
            context.changeProperty(()->wrapper.wrap(book, syncControl).setName("Book #2"));

        });

        return true;
    }

    @Override
    protected boolean wrapperSyncExecutionWithFailure(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // when
        withBookDo(book->{

            context.runGiven();

            // when - running synchronous
            val syncControl = SyncControl.control().withCheckRules(); // enforce rules

            assertThrows(DisabledException.class, ()->{
                wrapper.wrap(book, syncControl).setName("Book #2"); // should fail with DisabledException
            });

        });

        return false;
    }

    // -- TESTS - WRAPPER ASYNC

    @Override
    protected boolean wrapperAsyncExecutionNoRules(
            final PublishingTestContext context) throws InterruptedException, ExecutionException, TimeoutException {

        context.bind(commitListener);

        // given
        val asyncControl = returningVoid().withSkipRules(); // don't enforce rules

        // when

        withBookDo(book->{

            context.runGiven();

            // when - running asynchronous
            wrapper.asyncWrap(book, asyncControl)
            .setName("Book #2");

        });

        asyncControl.getFuture().get(10, TimeUnit.SECONDS);

        return true;
    }

    @Override
    protected boolean wrapperAsyncExecutionWithFailure(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // when
        withBookDo(book->{



            // when - running synchronous
            val asyncControl = returningVoid().withCheckRules(); // enforce rules

            assertThrows(DisabledException.class, ()->{
                // should fail with DisabledException (synchronous) within the calling Thread
                wrapper.asyncWrap(book, asyncControl).setName("Book #2");

                fail("unexpected code reach");
            });

        });

        return false;
    }

    // -- TEST SETUP

    private void setupBookForJpa() {

        val em = jpaSupport.getEntityManagerElseFail(JpaBook.class);

        // cleanup
        fixtureScripts.runPersona(JpaTestDomainPersona.PurgeAll);

        // given Inventory with 1 Book

        val products = new HashSet<JpaProduct>();

        products.add(JpaBook.of(
                "Sample Book", "A sample book for testing.", 99.,
                "Sample Author", "Sample ISBN", "Sample Publisher"));

        val inventory = JpaInventory.of("Sample Inventory", products);
        em.persist(inventory);

        inventory.getProducts().forEach(product->{
            em.persist(product);

            _Probe.errOut("PROD ID: %s", product.getId());

        });

        //fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);

        em.flush();
    }

    @SneakyThrows
    private void withBookDo(final CheckedConsumer<JpaBook> bookConsumer) {

        val book = repository.allInstances(JpaBook.class).listIterator().next();
        bookConsumer.accept(book);

    }


}
