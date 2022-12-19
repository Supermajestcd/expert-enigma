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

package org.apache.isis.core.runtime.logging;


public abstract class Log4jLogger {
    
    public static final String PROPERTY_ROOT = "isis.logging.";
    
    private org.apache.log4j.Logger logger;
    private final org.apache.log4j.Level level;

    public Log4jLogger() {
        this(org.apache.log4j.Level.DEBUG);
    }

    public Log4jLogger(final String level) {
        this.level = org.apache.log4j.Level.toLevel(level);
    }

    public Log4jLogger(final org.apache.log4j.Level level) {
        this.level = level;
    }

    protected abstract Class<?> getDecoratedClass();

    public void log(final String message) {
        logger().log(level, message);
    }

    public void log(final String request, final Object result) {
        log(request + "  -> " + result);
    }

    private org.apache.log4j.Logger logger() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(getDecoratedClass());
        }
        return logger;
    }
}
