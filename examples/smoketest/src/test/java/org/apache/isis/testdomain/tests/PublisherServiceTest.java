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
package org.apache.isis.testdomain.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScripts;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.val;

/**
 * Depends on {@link JdoBootstrappingTest_usingFixtures} to succeed.
 */
@SpringBootTest(
		classes = { 
				JdoTestDomainModule.class, 
				PublisherServiceTest.PublisherServiceProbe.class
		}, 
		properties = {
				"logging.config=log4j2-test.xml",
				"logging.level.org.apache.isis.incubator.IsisPlatformTransactionManagerForJdo=DEBUG",
				// "isis.reflector.introspector.parallelize=false",
				// "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
	})
//@Transactional //XXX this test is non transactional
class PublisherServiceTest {

	@Inject private RepositoryService repository;
	@Inject private FixtureScripts fixtureScripts;
	@Inject private PublisherServiceProbe publisherService;

	@BeforeEach
	void setUp() {
		
		val transactionTemplate = IsisContext.createTransactionTemplate();
		transactionTemplate.execute(status -> {

			// cleanup
			fixtureScripts.runBuilderScript(JdoTestDomainPersona.PurgeAll.builder());

			// given
			fixtureScripts.runBuilderScript(JdoTestDomainPersona.InventoryWith1Book.builder());
			
			return null;
			
		});

	}

	@Test
	void publisherServiceShouldBeAwareOfInventoryChanges() {

		// given
		val book = repository.allInstances(Book.class).listIterator().next();

		// when - running within its own transactional boundary
		val transactionTemplate = IsisContext.createTransactionTemplate();
		transactionTemplate.execute(status -> {

			publisherService.clearHistory();
			book.setName("Book #2");
			repository.persist(book);
			
			// then - before the commit
			assertEquals("", publisherService.getHistory());
				
			return null;
		});
		
		// then - after the commit
		assertEquals("publishedObjects=created=0,deleted=0,loaded=0,updated=1,modified=1,",
				publisherService.getHistory());

	}

	// -- HELPER

	@Singleton
	public static class PublisherServiceProbe implements PublisherService {

		private StringBuilder history = new StringBuilder();

		void clearHistory() {
			history = new StringBuilder();
		}

		String getHistory() {
			return history.toString();
		}

		@Override
		public void publish(Execution<?, ?> execution) {
			history.append("execution=").append(execution).append(",");
		}

		@Override
		public void publish(PublishedObjects publishedObjects) {
			history.append("publishedObjects=")
			.append("created=").append(publishedObjects.getNumberCreated()).append(",")
			.append("deleted=").append(publishedObjects.getNumberDeleted()).append(",")
			.append("loaded=").append(publishedObjects.getNumberLoaded()).append(",")
			.append("updated=").append(publishedObjects.getNumberUpdated()).append(",")
			.append("modified=").append(publishedObjects.getNumberPropertiesModified()).append(",")
			;
		}

	}

}
