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


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * An application-scoped CDI managed bean named which serves as an injectable
 * component for managing configuration parameters.
 *
 * @author Phillip Ross
 */
@SuppressFBWarnings({"PI_DO_NOT_REUSE_PUBLIC_IDENTIFIERS_CLASS_NAMES"})
@ApplicationScoped
public class Configuration implements Serializable {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    /** The default name of the system property referencing to the location of the configuration file. */
    public static final String DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME = "co.luminositylabs.configFile";

    /** The default name of the configuration file. */
    private static final String DEFAULT_CONFIG_FILENAME = "co.luminositylabs.config.properties";

    private static final long serialVersionUID = 7848558640626834259L;

    /** A lock which handles concurrent access to the properties. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The read lock used to handle concurrent access to the properties. */
    private final Lock readLock = lock.readLock();

    /** The write lock used to handle concurrent access to the properties. */
    private final Lock writeLock = lock.writeLock();

    /** Underlying storage object for the properties. */
    private final Properties properties = new Properties();


    /** Instantiates a new configuration object. */
    public Configuration() {
    }


    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        readLock.lock();
        try {
            return (Properties)properties.clone();
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Gets the properties that begin with a specified prefix.
     *
     * @param prefix the prefix
     * @return the properties
     */
    public Properties getPropertiesWithPrefix(final String prefix) {
        readLock.lock();
        try {
            Properties propertiesWithPrefix = new Properties();
            for (String propertyName : properties.stringPropertyNames()) {
                if (propertyName.startsWith(prefix)) {
                    propertiesWithPrefix.setProperty(propertyName, properties.getProperty(propertyName));
                }
            }
            return propertiesWithPrefix;
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Clears existing configuration properties replaces with the specified properties.
     *
     * @param properties the properties
     */
    public void setProperties(final Properties properties) {
        writeLock.lock();
        try {
            this.properties.clear();
            this.properties.putAll(properties);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * Sets the specified property to the specified value.
     *
     * @param propertyName the property name to be set
     * @param propertyValue the property value to set the property to
     */
    public void setProperty(final String propertyName, final String propertyValue) {
        writeLock.lock();
        try {
            properties.setProperty(propertyName, propertyValue);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * Gets the property value with the specified name.
     *
     * @param propertyName the name of the property to get
     * @return the value of the property retrieved
     */
    public String getProperty(final String propertyName) {
        readLock.lock();
        try {
            return properties.getProperty(propertyName);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Gets the property value with the specified name.
     * If the property does not exist, the specified default value is returned.
     *
     * @param propertyName the name of the property to get
     * @param defaultValue the value returned if the property does not exist
     * @return the value of the property retrieved
     */
    public String getProperty(final String propertyName, final String defaultValue) {
        readLock.lock();
        try {
            return properties.getProperty(propertyName, defaultValue);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Delegates to {@link java.util.Properties#get(Object)}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the specified key is null
     */
    public Object get(final Object key) {
        readLock.lock();
        try {
            return properties.get(key);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Delegates to {@link java.util.Properties#getOrDefault(Object, Object)}.
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the value to be returned if the associated property value is null
     * @return the value to which the specified key is mapped, or
     *         {@code defaultValue} if this map contains no mapping for the key
     */
    public Object getOrDefault(final Object key, final Object defaultValue) {
        readLock.lock();
        try {
            return properties.getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * Delegates to {@link java.util.Properties#put(Object, Object)}.
     *
     * @param key the hashtable key
     * @param value the value
     * @return the previous value of the specified key in this hashtable, or
     *         <code>null</code> if it did not have one
     * @exception  NullPointerException if the key or value is
     *             <code>null</code>
     */
    public Object put(final Object key, final Object value) {
        writeLock.lock();
        try {
            return properties.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * Delegates to {@link java.util.Properties#putAll(Map)}.
     *
     * @param map mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(final Map<?, ?> map) {
        writeLock.lock();
        try {
            properties.putAll(map);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * Delegates to {@link java.util.Properties#putIfAbsent(Object, Object)}.
     *
     * @param key the hashtable key
     * @param value the value
     * @return the previous value of the specified key in this hashtable, or
     *         <code>null</code> if it did not have one
     * @exception  NullPointerException if the key or value is
     *             <code>null</code>
     */
    public Object putIfAbsent(final Object key, final Object value) {
        writeLock.lock();
        try {
            return properties.putIfAbsent(key, value);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * Reads in the configuration properties.
     *
     * If the {@code co.luminositylabs.configFile} system property exists, the
     * value of this property is used as the path to read the configuration
     * from.  If the property does not exist, a path
     * named {@code co.luminositylabs.config.properties} is used.
     * <p>
     * If the configuration pathname does NOT represent an absolute path, the
     * pathname will be treated as a relative path, and the file will be
     * searched for in a series of relative locations:
     * <ol>
     * <li>First, the file will be searched for at the path relative to the current working directory.
     * <li>Second, the file will be searched for at the path relative to the home directory.
     * <li>Lastly, the file will be searched for at the path relative to the classpath.
     * </ol>
     * <p>
     * If the configuration file is not found, an {@code IOException} is thrown.
     */
    @PostConstruct
    public void readProperties() {
        writeLock.lock();
        try {
            logger.debug("default config file path property name: {}", DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME);
            properties.clear();
            final String configFileName = configFileName();
            logger.debug("Looking for {} in filesystem...", configFileName);
            Path configFilePath = Paths.get(configFileName);
            // Check to see if this full path points to an existing location on the filesystem.
            if (Files.exists(configFilePath)) {
                logger.debug("file exists in filesystem at {}", configFilePath);
                try (InputStream inputStream = Files.newInputStream(configFilePath)) {
                    properties.load(inputStream);
                } catch (IOException ioe) {
                    logger.debug("Error occurred while trying to read properties from {} on filesystem.",
                            configFilePath,
                            ioe
                    );
                }
            } else {
                String homeDirectoryPathName = System.getProperty("user.home");
                if (homeDirectoryPathName != null) {
                    logger.debug("could not find file... checking home directory.");
                    Path homeDirectoryPath = Paths.get(homeDirectoryPathName);
                    Path configFilePathRelativeToHomeDirectory = homeDirectoryPath.resolve(configFilePath);
                    if (Files.exists(configFilePathRelativeToHomeDirectory)) {
                        logger.debug("Found config file in home directory ({})", configFilePathRelativeToHomeDirectory);
                        try (InputStream inputStream = Files.newInputStream(configFilePathRelativeToHomeDirectory)) {
                            readPropertiesFromInputStream(inputStream);
                        } catch (FileNotFoundException e) {
                            logger.debug("Did not find config file in home directory");
                        } catch (IOException ioe) {
                            logger.debug(
                                    "Error occurred while trying to read properties from {}.",
                                    configFilePathRelativeToHomeDirectory,
                                    ioe
                            );
                        }
                    } else {
                        logger.debug("Did not find config file in home directory");
                        logger.debug("Looking for {} in classpath...", configFilePath);
                        // When the file does not exist at the specified path in the
                        // filesystem... check to see if it exists relative to the classpath.
                        try (InputStream inputStream = Thread.currentThread()
                                .getContextClassLoader()
                                .getResourceAsStream(configFileName)) {
                            if (inputStream != null) {
                                logger.debug("file exists in classpath at {}", configFilePath);
                                readPropertiesFromInputStream(inputStream);
                            } else {
                                logger.debug("Did not find file ({}) in classpath", configFilePath);
                            }
                        } catch (IOException ioe) {
                            logger.debug(
                                    "Error occurred while trying to read properties from {}.",
                                    configFileName,
                                    ioe
                            );
                        }
                    }
                } else {
                    logger.debug("user.home system property was not found.");
                }
            }
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * Get the name of the config file.
     *
     * The config file name is determined by first searching for a system property specified by the
     * DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME constant.  When the system property with that name exists, the value of
     * that system property is returned as the full path name of the config file.  When the system property with that
     * name does not exist, then a default config file name specified by the DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME
     * constant is returned.  This default config file name only specifies the file name and NOT the full path.
     *
     * @return the name of the config file.
     */
    private String configFileName() {
        // Check the system properties to see if there is a property which specifies the full path of a config file.
        logger.debug(
                "Searching system properties for a specified config file path ({})",
                DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME
        );
        String configFileName = System.getProperty(DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME);
        if (configFileName != null) {
            logger.debug(
                    "Found system property ({}={})", DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME, configFileName
            );
        } else {
            logger.debug("Did not find system property ({})", DEFAULT_CONFIG_FILE_PATH_PROPERTY_NAME);
            // If the config file path was not specified as a system property,
            // assume a default file name without a path.
            logger.debug(
                    "Assuming a default name for the config file without a path. ({})", DEFAULT_CONFIG_FILENAME
            );
            configFileName = DEFAULT_CONFIG_FILENAME;
        }
        return configFileName;
    }


    /**
     * Reads properties from the specified input stream.
     *
     * @param inputStream the input stream to be read
     * @throws IOException when an I/O exception occurrs
     */
    private void readPropertiesFromInputStream(final InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "Unable to read properties from null input stream");
        logger.debug("Reading properties from input stream.");
        properties.load(inputStream);
        logger.debug("Read properties from input stream: {}", properties);
    }


}
