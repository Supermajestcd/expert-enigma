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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.request.Request;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.ImpersonatedUserHolder;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.security._testing.InteractionService_forTesting;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGeneratorDefault;

public class AuthenticatedWebSessionForCauseway_Authenticate {

    @Rule
    public final JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private AuthenticationManager authMgr;

    @Mock protected Request mockRequest;
    @Mock protected Authenticator mockAuthenticator;
    @Mock protected InteractionService mockInteractionService;
    @Mock protected ImpersonatedUserHolder mockImpersonatedUserHolder;
    @Mock protected InteractionLayerTracker mockInteractionLayerTracker;

    protected AuthenticatedWebSessionForCauseway webSession;
    private MetaModelContext mmc;

    @Before
    public void setUp() throws Exception {

        mmc = MetaModelContext_forTesting.builder()
                .singleton(mockInteractionService)
                .singleton(mockImpersonatedUserHolder)
                .build();

        authMgr = new AuthenticationManager(
                Collections.singletonList(mockAuthenticator),
                new InteractionService_forTesting(),
                new RandomCodeGeneratorDefault(),
                Optional.empty(),
                Collections.emptyList());

        context.checking(new Expectations() {
            {
                allowing(mockInteractionLayerTracker).currentInteractionContext();
                will(returnValue(Optional.of(InteractionContextFactory.testing())));

                allowing(mockInteractionService)
                .run(with(InteractionContextFactory.testing()), with(any(ThrowingRunnable.class)));

                allowing(mockInteractionService)
                .runAnonymous(with(any(ThrowingRunnable.class)));

                // ignore

                // must provide explicit expectation, since Locale is final.
                allowing(mockRequest).getLocale();
                will(returnValue(Locale.getDefault()));

                // stub everything else out
                ignoring(mockRequest);
            }
        });

    }

    protected void setupWebSession() {
        webSession = new AuthenticatedWebSessionForCauseway(mockRequest) {
            private static final long serialVersionUID = 1L;

            {
                metaModelContext = mmc;
            }

            @Override
            public AuthenticationManager getAuthenticationManager() {
                return authMgr;
            }
        };
    }



    @Test
    public void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {

        context.checking(new Expectations() {
            {
                oneOf(mockImpersonatedUserHolder).getUserMemento();
                will(returnValue(Optional.empty()));
                oneOf(mockAuthenticator).canAuthenticate(AuthenticationRequestPassword.class);
                will(returnValue(true));
                oneOf(mockAuthenticator).authenticate(with(any(AuthenticationRequest.class)), with(any(String.class)));
                will(returnValue(InteractionContextFactory.testing()));
            }
        });

        setupWebSession();

        // when
        assertThat(webSession.authenticate("jsmith", "secret"), is(true));

        // then
        assertThat(webSession.getAuthentication(), is(not(nullValue())));
    }

    @Test
    public void delegatesToAuthenticationManagerAndHandlesIfNotAuthenticated() {
        context.checking(new Expectations() {
            {
                oneOf(mockAuthenticator).canAuthenticate(AuthenticationRequestPassword.class);
                will(returnValue(true));
                oneOf(mockAuthenticator).authenticate(with(any(AuthenticationRequest.class)), with(any(String.class)));
                will(returnValue(null));
            }
        });

        setupWebSession();

        assertThat(webSession.authenticate("jsmith", "secret"), is(false));
        assertThat(webSession.getAuthentication(), is(nullValue()));
    }

}