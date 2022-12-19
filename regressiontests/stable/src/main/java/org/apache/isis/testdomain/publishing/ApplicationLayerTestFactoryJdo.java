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
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

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
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.publishing.ApplicationLayerTestFactoryAbstract.PreCommitListener;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@Import({
    PreCommitListener.class
})
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationLayerTestFactoryJdo
extends ApplicationLayerTestFactoryAbstract {

    private final RepositoryService repository;
    private final WrapperFactory wrapper;
    private final ObjectManager objectManager;
    private final FixtureScripts fixtureScripts;
    private final PreCommitListener preCommitListener;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final InteractionService interactionService;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final TransactionService transactionService;

    @Named("transaction-aware-pmf-proxy")
    private final PersistenceManagerFactory pmf;

    // -- TESTS - WRAPPER SYNC

    @Override
    protected boolean programmaticExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();

        preCommitListener.setVerifier(verifier);

        withBookDoTransactional(book->{

            given.run();

            // when - direct change (circumventing the framework)
            book.setName("Book #2");
            repository.persist(book);

        });

        preCommitListener.setVerifier(null);

        // This test does not trigger command or execution publishing, however it does trigger
        // entity-change-publishing.

        // then
        verifier.accept(VerificationStage.POST_COMMIT_WHEN_PROGRAMMATIC);

        return true;
    }

    // -- TESTS - INTERACTION API

    @Override
    protected boolean interactionApiExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();

        // when
        withBookDoTransactional(book->{

            given.run();

            preCommitListener.setVerifier(verifier);

            // when
            val bookAdapter = objectManager.adapt(book);
            val propertyInteraction = PropertyInteraction.start(bookAdapter, "name", Where.OBJECT_FORMS);
            val managedProperty = propertyInteraction.getManagedPropertyElseThrow(__->_Exceptions.noSuchElement());
            val propertyModel = managedProperty.startNegotiation();
            val propertySpec = managedProperty.getSpecification();
            propertyModel.getValue().setValue(ManagedObject.of(propertySpec, "Book #2"));
            propertyModel.submit();

        });

        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.POST_COMMIT);

        return true;
    }

    // -- TESTS - WRAPPER SYNC

    @Override
    protected boolean wrapperSyncExecutionNoRules(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();

        // when
        withBookDoTransactional(book->{

            given.run();

            preCommitListener.setVerifier(verifier);

            // when - running synchronous
            val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
            wrapper.wrap(book, syncControl).setName("Book #2");

            preCommitListener.setVerifier(null);

        });

        // then
        verifier.accept(VerificationStage.POST_COMMIT);

        return true;
    }

    @Override
    protected boolean wrapperSyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();

        // when
        withBookDoTransactional(book->{

            given.run();

            preCommitListener.setVerifier(verifier);

            // when - running synchronous
            val syncControl = SyncControl.control().withCheckRules(); // enforce rules

            assertThrows(DisabledException.class, ()->{
                wrapper.wrap(book, syncControl).setName("Book #2"); // should fail with DisabledException
            });

            preCommitListener.setVerifier(null);

        });


        // then
        verifier.accept(VerificationStage.FAILURE_CASE);

        return false;
    }

    // -- TESTS - WRAPPER ASYNC

    @Override
    protected boolean wrapperAsyncExecutionNoRules(
            final Runnable given,
            final Consumer<VerificationStage> verifier) throws InterruptedException, ExecutionException, TimeoutException {

        // given
        setupBookForJdo();
        val asyncControl = returningVoid().withSkipRules(); // don't enforce rules

        // when

        withBookDoTransactional(book->{

            given.run();

            preCommitListener.setVerifier(verifier);

            // when - running asynchronous
            wrapper.asyncWrap(book, asyncControl)
            .setName("Book #2");

        });

        asyncControl.getFuture().get(10, TimeUnit.SECONDS);

        preCommitListener.setVerifier(null);

        // then
        verifier.accept(VerificationStage.POST_COMMIT);

        return true;
    }

    @Override
    protected boolean wrapperAsyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        // given
        setupBookForJdo();

        // when
        withBookDoTransactional(book->{

            given.run();

            preCommitListener.setVerifier(verifier);

            // when - running synchronous
            val asyncControl = returningVoid().withCheckRules(); // enforce rules

            assertThrows(DisabledException.class, ()->{
                // should fail with DisabledException (synchronous) within the calling Thread
                wrapper.asyncWrap(book, asyncControl).setName("Book #2");

                fail("unexpected code reach");
            });

            preCommitListener.setVerifier(null);

        });

        // then
        verifier.accept(VerificationStage.FAILURE_CASE);

        return false;
    }

    // -- TEST SETUP

    private void setupBookForJdo() {

        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            val pm = pmf.getPersistenceManager();

            // cleanup
            fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

            // given Inventory with 1 Book

            val products = new HashSet<JdoProduct>();

            products.add(JdoBook.of(
                    "Sample Book", "A sample book for testing.", 99.,
                    "Sample Author", "Sample ISBN", "Sample Publisher"));

            val inventory = JdoInventory.of("Sample Inventory", products);
            pm.makePersistent(inventory);

            inventory.getProducts().forEach(product->{
                val prod = pm.makePersistent(product);

                _Probe.errOut("PROD ID: %s", JDOHelper.getObjectId(prod));

            });

            //fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

            pm.flush();

        });
    }

    private void withBookDoTransactional(final CheckedConsumer<JdoBook> transactionalBookConsumer) {

        xrayEnterTansaction(Propagation.REQUIRES_NEW);

        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->{
            val book = repository.allInstances(JdoBook.class).listIterator().next();
            transactionalBookConsumer.accept(book);

        })
        .optionalElseFail();

        xrayExitTansaction();
    }


}
