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
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Vetoed;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.factory.InstanceUtil;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.DataNucleusLifeCycleHelper;
import org.apache.isis.persistence.jdo.datanucleus5.datanucleus.DataNucleusPropertiesAware;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.query.JdoQueryFacet;
import org.apache.isis.core.runtime.context.IsisContext;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Vetoed
@Log4j2
public class DataNucleusApplicationComponents5 {

    private final Set<String> persistableClassNameSet;
    private final IsisConfiguration configuration;
    private final Map<String, Object> datanucleusProps;

    @Getter private PersistenceManagerFactory persistenceManagerFactory;

    public DataNucleusApplicationComponents5(
            final IsisConfiguration configuration,
            final Map<String, Object> datanucleusProps,
            final Set<String> persistableClassNameSet) {
        
        this.configuration = configuration;
        this.datanucleusProps = datanucleusProps;
        this.persistableClassNameSet = persistableClassNameSet;
        
        persistenceManagerFactory = createPmfAndSchemaIfRequired(
                this.persistableClassNameSet, this.datanucleusProps);
    }

    /**
     * Marks the end of DataNucleus' life-cycle. Purges any state associated with DN.
     * Subsequent calls have no effect.
     *
     * @since 2.0.0
     */
    public void shutdown() {
        if(persistenceManagerFactory != null) {
            DataNucleusLifeCycleHelper.cleanUp(persistenceManagerFactory);
            persistenceManagerFactory = null;
        }
    }

    static PersistenceManagerFactory newPersistenceManagerFactory(Map<String, Object> datanucleusProps) {
        try {
            // this is where DN will throw an exception if we pass it any config props it doesn't like the look of.
            // we want to fail, but let's make sure that the error is visible to help the developer
            return JDOHelper.getPersistenceManagerFactory(datanucleusProps, IsisContext.getClassLoader());
        } catch(JDOUserException ex) {
            log.fatal(ex);
            throw ex;
        }
    }

    // REF: http://www.datanucleus.org/products/datanucleus/jdo/schema.html
    private PersistenceManagerFactory createPmfAndSchemaIfRequired(
            final Set<String> persistableClassNameSet, 
            final Map<String, Object> datanucleusProps) {

        final DNStoreManagerType dnStoreManagerType = DNStoreManagerType.typeOf(datanucleusProps);

        PersistenceManagerFactory persistenceManagerFactory;

        if(dnStoreManagerType.isSchemaAware()) {

            // rather than reinvent too much of the wheel, we reuse the same property that DN would check
            // for if it were doing the auto-creation itself (read from isis.properties)
            final boolean createSchema = isSet(datanucleusProps, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL);

            if(createSchema) {

                configureAutoCreateSchema(datanucleusProps);
                persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
                createSchema(persistenceManagerFactory, persistableClassNameSet, datanucleusProps);

            } else {
                persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
            }

        } else {

            // we *DO* use DN's eager loading (autoStart), because it seems that DN requires this (for neo4j at least)
            // otherwise NPEs occur later.

            configureAutoStart(persistableClassNameSet, datanucleusProps);
            persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
        }

        return persistenceManagerFactory;

    }

    private void configureAutoCreateSchema(final Map<String, Object> datanucleusProps) {
        // unlike v1, we DO now use we DN's eager loading for schema (ie set to PROPERTY_SCHEMA_AUTOCREATE_ALL to true).
        //
        // the mechanism in v1 was to register a listener to create a schema just-in-time
        // (CreateSchemaObjectFromClassMetadata); this does seem to be needed still if running the tests within an IDE
        // but it is called too late if running from mvn.  Luckily, it seems that DN's autocreate works within mvn.
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL, "true");

        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_DATABASE, "false");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_TABLES, "true");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_COLUMNS, "true");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_CONSTRAINTS, "true");
    }

    private void configureAutoStart(
            final Set<String> persistableClassNameSet, 
            final Map<String, Object> datanucleusProps) {

        final String persistableClassNames =
                stream(persistableClassNameSet).collect(Collectors.joining(","));

        // ref: http://www.datanucleus.org/products/datanucleus/jdo/autostart.html
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_MECHANISM, "Classes");
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_MODE, "Checked");
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_CLASSNAMES, persistableClassNames);
    }

    private void createSchema(
            final PersistenceManagerFactory persistenceManagerFactory,
            final Set<String> persistableClassNameSet,
            final Map<String, Object> datanucleusProps) {

        JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory) persistenceManagerFactory;
        final PersistenceNucleusContext nucleusContext = jdopmf.getNucleusContext();
        final SchemaAwareStoreManager schemaAwareStoreManager = 
                (SchemaAwareStoreManager)nucleusContext.getStoreManager();

        final MetaDataManager metaDataManager = nucleusContext.getMetaDataManager();

        registerMetadataListener(metaDataManager, datanucleusProps);

        if(_NullSafe.isEmpty(persistableClassNameSet)) {
            return; // skip
        }

        schemaAwareStoreManager.createSchemaForClasses(persistableClassNameSet, asProperties(datanucleusProps));
    }

    private boolean isSet(final Map<String, Object> props, final String key) {
        return Boolean.parseBoolean( ""+props.get(key) );
    }

    private void registerMetadataListener(
            final MetaDataManager metaDataManager,
            final Map<String, Object> datanucleusProps) {
        final MetaDataListener listener = createMetaDataListener();
        if(listener == null) {
            return;
        }

        if(listener instanceof DataNucleusPropertiesAware) {
            ((DataNucleusPropertiesAware) listener).setDataNucleusProperties(datanucleusProps);
        }


        // and install the listener for any classes that are lazily loaded subsequently
        // (shouldn't be any, this is mostly backwards compatibility with previous design).
        metaDataManager.registerListener(listener);
    }

    private MetaDataListener createMetaDataListener() {
        final String classMetadataListenerClassName = configuration.getPersistence().getJdoDatanucleus().getClassMetadataLoadedListener();
        return classMetadataListenerClassName != null
                ? InstanceUtil.createInstance(classMetadataListenerClassName, MetaDataListener.class)
                        : null;
    }


    private static Properties asProperties(final Map<String, Object> props) {
        final Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    static void catalogNamedQueries(
            Set<String> persistableClassNames, SpecificationLoader specificationLoader) {
        
        val namedQueryByName = _Maps.<String, JdoNamedQuery>newHashMap();
        for (val persistableClassName: persistableClassNames) {
            val spec = specificationLoader.loadSpecification(ObjectSpecId.of(persistableClassName));
            val jdoQueryFacet = spec.getFacet(JdoQueryFacet.class);
            if (jdoQueryFacet == null) {
                continue;
            }
            for (val namedQuery : jdoQueryFacet.getNamedQueries()) {
                namedQueryByName.put(namedQuery.getName(), namedQuery);
            }
        }
    }



}
