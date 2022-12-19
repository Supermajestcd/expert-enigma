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
package org.apache.isis.incubator.viewer.vaadin.ui.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.stereotype.Component;

import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.session.IsisSessionFactory.ThrowingRunnable;
import org.apache.isis.core.security.authentication.standard.SimpleSession;
import org.apache.isis.incubator.viewer.vaadin.ui.pages.login.VaadinLoginView;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Hooks into Vaadin's routing, such that unauthorized access is redirected to the login view.
 * @since Mar 9, 2020
 *
 */
@Component
@Log4j2
public class VaadinAuthenticationHandler implements VaadinServiceInitListener {

    private static final long serialVersionUID = 1L;
    
    @Inject private transient IsisSessionFactory isisSessionFactory; 

    @Override
    public void serviceInit(ServiceInitEvent event) {
        
        log.debug("service init event {}", event.getSource());
        
        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(this::beforeEnter); 
            uiEvent.getUI().addBeforeLeaveListener(this::beforeLeave);
        });
    }

    /**
     * @param userName
     * @param secret
     * @return whether login was successful
     */
    public boolean loginToSession(String userName, String secret) {
        log.warn("logging in {} not implemented yet", userName);
        return false;

     // TODO actual authentication to be done here ...        
//        AuthSessionStoreUtil.put(new SimpleSession(userName, Collections.emptyList()));
//        return true;
    }
    
    /** @deprecated early development only */
    public boolean loginToSessionAsSven() {
        log.debug("logging in as Sven");
        AuthSessionStoreUtil.putSven();
        return true;
    }
    
    public void logoutFromSession() {
        AuthSessionStoreUtil.get()
        .ifPresent(authSession->{
            log.debug("logging out {}", authSession.getUserName());
            AuthSessionStoreUtil.clear();
        });
        VaadinSession.getCurrent().close();
        isisSessionFactory.closeSessionStack();
    } 
    
    /**
     * Executes a piece of code in a new (possibly nested) IsisSession, using the 
     * current AuthenticationSession, as, at this point, should be stored in the 
     * current VaadinSession.
     * 
     * @param callable - the piece of code to run
     * 
     */
    public <R> R callAuthenticated(Callable<R> callable) {
        return AuthSessionStoreUtil.get()
                .map(authSession->isisSessionFactory.callAuthenticated(authSession, callable))
                .orElse(null); // TODO redirect to login
    }
    
    /**
     * Variant of {@link #callAuthenticated(Callable)} that takes a runnable.
     * @param runnable
     */
    public void runAuthenticated(ThrowingRunnable runnable) {
        final Callable<Void> callable = ()->{runnable.run(); return null;};
        callAuthenticated(callable);
    }
    

    // -- HELPER

    private void beforeEnter(BeforeEnterEvent event) {
        val targetView = event.getNavigationTarget();
        log.debug("detected a routing event to {}", targetView);
        
        val authSession = AuthSessionStoreUtil.get().orElse(null);
        if(authSession!=null) {
            isisSessionFactory.openSession(authSession);
            return; // access granted
        }
        // otherwise redirect to login page
        if(!VaadinLoginView.class.equals(targetView)) {
            event.rerouteTo(VaadinLoginView.class);
        }
    }
    
    private void beforeLeave(BeforeLeaveEvent event) {
        //isisSessionFactory.closeSessionStack();
    }


    


}
