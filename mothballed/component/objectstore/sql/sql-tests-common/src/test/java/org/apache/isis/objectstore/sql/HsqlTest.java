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
package org.apache.isis.objectstore.sql;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import org.apache.isis.core.unittestsupport.files.Files;
import org.apache.isis.core.unittestsupport.files.Files.Recursion;
import org.apache.isis.objectstore.sql.common.Data;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestData;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestFixtures;

/**
 * @author Kevin kevin@kmz.co.za
 * 
 *         This test implementation uses the HyperSQL database engine to perform "serverless" tests of data creation and
 *         reloading.
 * 
 * 
 * @version $Rev$ $Date$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HsqlTest extends SqlIntegrationTestData {

    @BeforeClass
    public static void deleteHsqlDbFiles() {
        Files.deleteFilesWithPrefix("hsql-db", "tests", Recursion.DONT_RECURSE);
    }

    @Override
    protected void testCreate() throws Exception {
        final SqlIntegrationTestFixtures sqlIntegrationTestFixtures = getSqlIntegrationTestFixtures();
        for (final String tableName : Data.getTableNames()) {
            sqlIntegrationTestFixtures.dropTable(tableName);
        }
        super.testCreate();
    }

    // @Override
    // protected String getSqlSetupString() {
    // return "COMMIT;";
    // }

    @Override
    public String getPropertiesFilename() {
        return "hsql.properties";
    }

    @Override
    public String getSqlTeardownString() {
        return "SHUTDOWN;";
    }

}
