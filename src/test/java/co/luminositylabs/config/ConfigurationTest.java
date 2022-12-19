/**
 * Copyright (c) 2016 Luminosity Labs LLC. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package co.luminositylabs.config;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class ConfigurationTest extends Arquillian {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationTest.class);

    @Inject
    private Configuration configuration;


    @Deployment
    public static Archive<?> createDeployment() {
        PomEquippedResolveStage resolver = Maven.resolver()
                .loadPomFromFile("pom.xml");
        File[] lb = resolver.importDependencies(ScopeType.COMPILE)
                .resolve("ch.qos.logback:logback-classic")
                .withTransitivity()
                .asFile();
        File[] tng = resolver.importDependencies(ScopeType.COMPILE)
                .resolve("org.testng:testng")
                .withTransitivity()
                .asFile();
        WebArchive jar = ShrinkWrap.create(WebArchive.class, "deployment.war")
                .addAsLibraries(lb)
                .addAsLibraries(tng)
                .addPackage(Configuration.class.getPackage())
                .addAsResource("co.luminositylabs.config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        logger.debug("deployable jar content: {}", jar.toString(true));
        return jar;
    }


    @Test(priority = -1)
    public void testConfigurationDefaultPropertiesLoad() {
        Assert.assertEquals(configuration.getProperty("testProperty1"), "1");
        Assert.assertEquals(configuration.getProperty("testProperty2"), "2");
        Assert.assertNull(configuration.getProperty("testProperty3"));
        Assert.assertEquals(configuration.getProperty("testProperty3", "3"), "3");
        Assert.assertEquals(configuration.getOrDefault("testProperty3", "3"), "3");
    }


    @Test
    public void testConfigurationBasicGetAndSetProperty() throws Exception {
        cleanProperties();
        Assert.assertEquals(configuration.getProperties().size(), 0);

        Assert.assertNull(configuration.getProperty("foo"));
        Assert.assertEquals(configuration.getProperty("foo", "a default"), "a default");
        configuration.setProperty("foo", "bar");
        Assert.assertEquals(configuration.getProperty("foo"), "bar");

        configuration.setProperties(new Properties());
        Assert.assertEquals(configuration.getProperties().size(), 0);
    }


    @Test
    public void testConfigurationGetPropertiesWithPrefix() {
        cleanProperties();
        Assert.assertEquals(configuration.getProperties().size(), 0);

        configuration.setProperty("aTest1", "yes");
        configuration.setProperty("aTest2", "yes");
        configuration.setProperty("aTest3", "yes");
        configuration.setProperty("someTest1", "no");
        Assert.assertTrue(configuration.getProperties().containsKey("aTest1"));
        Assert.assertTrue(configuration.getProperties().containsKey("aTest2"));
        Assert.assertTrue(configuration.getProperties().containsKey("aTest3"));
        Assert.assertTrue(configuration.getProperties().containsKey("someTest1"));
        Assert.assertTrue(configuration.getPropertiesWithPrefix("aTest").containsKey("aTest1"));
        Assert.assertTrue(configuration.getPropertiesWithPrefix("aTest").containsKey("aTest2"));
        Assert.assertTrue(configuration.getPropertiesWithPrefix("aTest").containsKey("aTest3"));
        Assert.assertFalse(configuration.getPropertiesWithPrefix("aTest").containsKey("someTest1"));
    }


    @Test
    public void testStandaloneProperties() {
        Properties properties = new Properties();
        Map<String, Map<String, String>> authorizationMap = new HashMap<>();
        properties.put("authorizationMap", authorizationMap);
        Assert.assertNotNull(properties.get("authorizationMap"));

        cleanProperties();
        Assert.assertEquals(configuration.getProperties().size(), 0);

        configuration.put("authorizationMap", authorizationMap);
        Assert.assertNotNull(configuration.get("authorizationMap"));
        Assert.assertEquals(configuration.get("authorizationMap"),authorizationMap);
    }


    @Test
    public void testPutAllProperties() {
        cleanProperties();
        Assert.assertEquals(configuration.getProperties().size(), 0);
        Map<String, Object> multiplePropertiesMap = new HashMap<>();
        multiplePropertiesMap.put("multi1", "multi1");
        multiplePropertiesMap.put("multi2", "multi2");
        multiplePropertiesMap.put("multi3", "multi3");
        configuration.putAll(multiplePropertiesMap);
        Assert.assertEquals(configuration.get("multi1"), "multi1");
        Assert.assertEquals(configuration.get("multi2"), "multi2");
        Assert.assertEquals(configuration.get("multi3"), "multi3");
        Assert.assertNull(configuration.get("multi4"));
    }


    @Test
    public void testPutIfAbsentProperties() {
        cleanProperties();
        Assert.assertEquals(configuration.getProperties().size(), 0);
        Assert.assertNull(configuration.putIfAbsent("test1", "test1"));
        Assert.assertEquals(configuration.get("test1"), "test1");
        Assert.assertEquals(configuration.putIfAbsent("test1", "test1-updated"), "test1");
        Assert.assertEquals(configuration.get("test1"), "test1");
    }


    public void cleanProperties() {
        logger.debug("Clearing properties from configuration.");
        Assert.assertNotNull(configuration);
        configuration.setProperties(new Properties());
        Assert.assertEquals(configuration.getProperties().size(), 0);
    }


}
