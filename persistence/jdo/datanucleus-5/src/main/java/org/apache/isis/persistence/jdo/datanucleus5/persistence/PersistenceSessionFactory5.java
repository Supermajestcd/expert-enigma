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

package org.apache.isis.persistence.jdo.datanucleus5.persistence;

import java.util.Map;
import java.util.Objects;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.listener.StoreLifecycleListener;

import org.datanucleus.PropertyNames;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.base._Blackhole;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.persistence.jdo.applib.fixturestate.FixturesInstalledState;
import org.apache.isis.persistence.jdo.applib.fixturestate.FixturesInstalledStateHolder;
import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.DataNucleusContextUtil;
import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.DataNucleusSettings;
import org.apache.isis.persistence.jdo.datanucleus5.entities.JdoEntityTypeRegistry;
import org.apache.isis.persistence.jdo.datanucleus5.lifecycles.JdoStoreLifecycleListenerForIsis;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 *
 * Factory for {@link PersistenceSession}.
 *
 */
@Service
@Named("isisJdoDn5.PersistenceSessionFactory5")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("JdoDN5")
@Singleton
@Log4j2
public class PersistenceSessionFactory5
implements PersistenceSessionFactory, FixturesInstalledStateHolder {
    
    @Inject private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;

    private final _Lazy<DataNucleusApplicationComponents5> applicationComponents = 
            _Lazy.threadSafe(this::createDataNucleusApplicationComponents);
    
    private StoreLifecycleListener storeLifecycleListener;
    private MetaModelContext metaModelContext;
    private IsisConfiguration configuration;
    private final JdoEntityTypeRegistry jdoEntityTypeRegistry = new JdoEntityTypeRegistry();

    @Getter(onMethod=@__({@Override})) 
    @Setter(onMethod=@__({@Override})) 
    FixturesInstalledState fixturesInstalledState;

    @Override
    public void init(MetaModelContext metaModelContext) {
        this.metaModelContext = metaModelContext;
        this.configuration = metaModelContext.getConfiguration();
        // need to eagerly build, ... must be completed before catalogNamedQueries().
        // Why? because that method causes entity classes to be loaded which register with DN's EnhancementHelper,
        // which are then cached in DN.  It results in our CreateSchema listener not firing.
        _Blackhole.consume(applicationComponents.get());
        
        this.storeLifecycleListener = new JdoStoreLifecycleListenerForIsis();
        metaModelContext.getServiceInjector().injectServicesInto(storeLifecycleListener);
    }


    @Override
    public boolean isInitialized() {
        return this.configuration != null;
    }

    private DataNucleusApplicationComponents5 createDataNucleusApplicationComponents() {

        val dnSettings = metaModelContext.getServiceRegistry().lookupServiceElseFail(DataNucleusSettings.class);
        val datanucleusProps = addDataNucleusPropertiesAsRequired(dnSettings);
        val typeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();
        val classesToBePersisted = jdoEntityTypeRegistry.getEntityTypes(typeRegistry);

        val dataNucleusApplicationComponents = new DataNucleusApplicationComponents5(
                configuration,
                datanucleusProps, 
                classesToBePersisted);
        
        return dataNucleusApplicationComponents;
    }

    @Override
    public void catalogNamedQueries() {
        val typeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();
        val classesToBePersisted = jdoEntityTypeRegistry.getEntityTypes(typeRegistry);
        
        if(log.isDebugEnabled()) {
            log.debug("Entity types discovered:");
            _NullSafe.stream(classesToBePersisted)
                .forEach(entityClassName->log.debug(" - {}", entityClassName));
        }
        
        DataNucleusApplicationComponents5.catalogNamedQueries(classesToBePersisted, 
                metaModelContext.getSpecificationLoader());
    }

    private Map<String, Object> addDataNucleusPropertiesAsRequired(DataNucleusSettings dnSettings) {
        
        val props = _Maps.<String, Object>newHashMap();
        props.putAll(dnSettings.getAsMap());
        DataNucleusContextUtil.putMetaModelContext(props, metaModelContext);

        String connectionFactoryName = (String) props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
        if(connectionFactoryName != null) {
            String connectionFactory2Name = (String) props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
            String transactionType = (String) props.get("javax.jdo.option.TransactionType");
            // extended logging
            if(transactionType == null) {
                log.info("found config properties to use non-JTA JNDI datasource ({})", connectionFactoryName);
                if(connectionFactory2Name != null) {
                    log.warn("found config properties to use non-JTA JNDI datasource ({}); second '-nontx' JNDI datasource also configured but will not be used ({})", connectionFactoryName, connectionFactory2Name);
                }
            } else {
                log.info("found config properties to use JTA JNDI datasource ({})", connectionFactoryName);
            }
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                log.error("found config properties to use JTA JNDI datasource ({}) but config properties for second '-nontx' JNDI datasource were *not* found", connectionFactoryName);
            } else {
                log.info("... and config properties for second '-nontx' JNDI datasource also found; {}", connectionFactory2Name);
            }
            // nothing further to do
        } else {
            // use JDBC connection properties; put if not present

            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            putIfNotPresent(props, "javax.jdo.option.ConnectionUserName", "sa");
            putIfNotPresent(props, "javax.jdo.option.ConnectionPassword", "");

            if(log.isInfoEnabled()) {
                log.info("using JDBC connection '{}'", 
                        props.get("javax.jdo.option.ConnectionURL"));
            }
        }
        
        return props;
    }

    private static void putIfNotPresent(
            Map<String, Object> props,
            String key,
            String value) {
        
        if(!props.containsKey(key)) {
            props.put(key, value);
        }
    }


    @PreDestroy
    public final void shutdown() {
        if(!isInitialized()) {
            return;
        }
        if(applicationComponents.isMemoized()) {
            applicationComponents.get().shutdown();
            applicationComponents.clear();
        }
        this.configuration = null;
        this.storeLifecycleListener = null;
    }

    @Override
    public PersistenceSession5 createPersistenceSession() {

        Objects.requireNonNull(applicationComponents.get(),
                () -> "PersistenceSession5 requires initialization. "+this.hashCode());

        val persistenceManagerFactory =
                applicationComponents.get().getPersistenceManagerFactory();

        return new PersistenceSession5(
                metaModelContext, 
                persistenceManagerFactory,
                storeLifecycleListener,
                this);
    }


}
