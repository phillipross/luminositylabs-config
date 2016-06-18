# Luminosity Labs Configuration Module
## This module consists of a suite of utilities for configuration management.

This module is packaged as a CDI bean archive.  It contains an application-scoped CDI managed bean named
`Configuration` which serves as an injectable component for managing configuration parameters.
 
The `Configuration` class loads the initial config immediately after the construction of the bean.
Although the bean is application-scoped, the point at which the bean is constructed may not occur immediately after the
application is deployed since the CDI container may lazily-load the bean.

If the `co.luminositylabs.configFile` system property exists, the value of this property is used as the path to read
the configuration from.  If the property does not exist, a path named `co.luminositylabs.config.properties` is used.
 
If the configuration pathname does NOT represent an absolute path, the pathname will be treated as a relative path,
and the file will be searched for in a series of relative locations:

1. First, the file will be searched for at the path relative to the current working directory.
2. Second, the file will be searched for at the path relative to the home directory.
3. Lastly, the file will be searched for at the path relative to the classpath.

If the configuration file is not found, an `IOException` is thrown.
