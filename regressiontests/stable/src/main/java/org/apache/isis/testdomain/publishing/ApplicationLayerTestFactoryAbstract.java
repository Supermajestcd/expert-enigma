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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.jupiter.api.DynamicTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.security.util.XrayUtil;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;

import lombok.Setter;
import lombok.val;

public abstract class ApplicationLayerTestFactoryAbstract {

    public static enum VerificationStage {
        PRE_COMMIT,
        POST_COMMIT,
        POST_COMMIT_WHEN_PROGRAMMATIC,
        FAILURE_CASE,
        POST_INTERACTION,
        POST_INTERACTION_WHEN_PROGRAMMATIC,
    }

    @FunctionalInterface
    private static interface InteractionTestRunner {
        boolean run(Runnable given, Consumer<VerificationStage> verifier) throws Exception;
    }

    @Service
    public static class PreCommitListener {

        @Setter private Consumer<VerificationStage> verifier;

        /** TRANSACTION END BOUNDARY */
        @EventListener(TransactionBeforeCompletionEvent.class)
        public void onPreCommit(final TransactionBeforeCompletionEvent event) {
            if(verifier!=null) {
                verifier.accept(VerificationStage.PRE_COMMIT);
            }
        }
    }

    // -- DEPENDENCIES

    protected abstract InteractionService getInteractionService();
    protected abstract TransactionService getTransactionService();

    // -- CREATE DYNAMIC TESTS

    public final List<DynamicTest> generateTests(
            final Runnable given,
            final Consumer<VerificationStage> verifier) {

        val dynamicTests = Can.<DynamicTest>of(

                interactionTest("Programmatic Execution",
                        given, verifier,
                        VerificationStage.POST_INTERACTION_WHEN_PROGRAMMATIC,
                        this::programmaticExecution),
                interactionTest("Interaction Api Execution",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::interactionApiExecution),
                interactionTest("Wrapper Sync Execution w/o Rules",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperSyncExecutionNoRules),
                interactionTest("Wrapper Sync Execution w/ Rules (expected to fail w/ DisabledException)",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperSyncExecutionWithFailure),
                interactionTest("Wrapper Async Execution w/o Rules",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperAsyncExecutionNoRules),
                interactionTest("Wrapper Async Execution w/ Rules (expected to fail w/ DisabledException)",
                        given, verifier,
                        VerificationStage.POST_INTERACTION,
                        this::wrapperAsyncExecutionWithFailure)
                );

        return XrayUi.isXrayEnabled()
                ? dynamicTests
                        .add(dynamicTest("wait for xray viewer", XrayUi::waitForShutdown))
                        .toList()
                : dynamicTests
                        .toList();

    }

    protected abstract boolean programmaticExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier);

    protected abstract boolean interactionApiExecution(
            final Runnable given,
            final Consumer<VerificationStage> verifier);

    protected abstract boolean wrapperSyncExecutionNoRules(
            final Runnable given,
            final Consumer<VerificationStage> verifier);

    protected abstract boolean wrapperSyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier);

    protected abstract boolean wrapperAsyncExecutionNoRules(
            final Runnable given,
            final Consumer<VerificationStage> verifier) throws InterruptedException, ExecutionException, TimeoutException;

    protected abstract boolean wrapperAsyncExecutionWithFailure(
            final Runnable given,
            final Consumer<VerificationStage> verifier);


    // -- HELPER

    private final DynamicTest interactionTest(
            final String displayName,
            final Runnable given,
            final Consumer<VerificationStage> verifier,
            final VerificationStage onSuccess,
            final InteractionTestRunner interactionTestRunner) {

        return dynamicTest(displayName, ()->{

            xrayAddTest(displayName);

            assertFalse(getInteractionService().isInInteraction());
            assert_no_initial_tx_context();

            final boolean isSuccesfulRun = getInteractionService().callAnonymous(()->{
                val currentInteraction = getInteractionService().currentInteraction();
                xrayEnterInteraction(currentInteraction);
                val result = interactionTestRunner.run(given, verifier);
                xrayExitInteraction();
                return result;
            });

            getInteractionService().closeInteractionLayers();

            if(isSuccesfulRun) {
                verifier.accept(onSuccess);
            }

        });
    }

    private final void assert_no_initial_tx_context() {
        val txState = getTransactionService().currentTransactionState();
        assertEquals(TransactionState.NONE, txState);
    }

    // -- XRAY

    private final void xrayAddTest(final String name) {

        val threadId = XrayUtil.currentThreadAsMemento();

        XrayUi.updateModel(model->{
            model.addContainerNode(
                    model.getThreadNode(threadId),
                    String.format("Test: %s", name));

        });

    }

    protected void xrayEnterTansaction(final Propagation propagation) {
    }

    protected void xrayExitTansaction() {
    }

    private void xrayEnterInteraction(final Optional<Interaction> currentInteraction) {
    }

    private void xrayExitInteraction() {
    }


}
