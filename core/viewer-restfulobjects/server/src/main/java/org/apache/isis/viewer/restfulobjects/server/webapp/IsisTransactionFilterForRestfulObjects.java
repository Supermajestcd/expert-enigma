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
package org.apache.isis.viewer.restfulobjects.server.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.TransactionalException;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.runtime.system.context.IsisContext;

//@WebFilter(servletNames= {"RestfulObjectsRestEasyDispatcher"}) //[ahuber] to support 
//Servlet 3.0 annotations @WebFilter, @WebListener or others 
//with skinny war deployment requires additional configuration, so for now we disable this annotation
public class IsisTransactionFilterForRestfulObjects implements Filter {

    private _Lazy<TransactionService> transactionService = 
            _Lazy.threadSafe(IsisContext::getTransactionService);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // too early ... transactionService = IsisContext.getTransactionService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {

        transactionService.get().executeWithinTransaction(()->{

            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw new TransactionalException("", e);
            }

        });

    }

    @Override
    public void destroy() {
    }


}
