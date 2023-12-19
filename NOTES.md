# Luminosity Labs Configuration Module

## Notes

### Integration Tests

The integration tests for this module are run by the maven failsafe plugin.  This plugin includes a configuration
 option name "argLine" which allows the specification of JVM arguments for the test runner.  Various arguments are
 needed for the integration tests to run successfully due to a combination of Payara5 and its included components
 support (or lack thereof) for the Java Platform Module System (JMPS) in Java9 and above.

#### Payara5

When the maven-failsafe-plugin runs the integration tests in Payara5 without the required JVM arguments, the
 bootstrapping of the Payara runtime by arquillian fails and the integration tests fail to run successfully.  The
 following notes attempt to document the details on which JVM arguments are necessary and which are not.

By default, the logging may not be verbose enough to show the warnings/errors containing hints as to the reasons for the
 failures.  In order to increase the verbosity of the logging, the <java.util.logging.config.file> element can be
 commented out in the failsafe plugin configuration system properties section, causing logging to be output to the
 console.

Two separate sections of notes are provided to explain the details when the arquillian test are run in Payara5 embedded
 edition versus when they are run in Payara5 micro edition.

##### Payara5 Embedded

Payara5 incorporates Hazelcast which requires specific JVM parameters to work property in a JPMS (Java9+) environment.
 The following warning message is displayed in the logging output when the maven failsafe plugin runs the integration
 tests by arquillian fails to deploy the application due to Hazelcast failures:
```
WARNING: Hazelcast is starting in a Java modular environment (Java 9 and newer) but without proper access to required Java packages. Use additional Java arguments to provide Hazelcast access to Java internal API. The internal API access is used to get the best performance results. Arguments to be used:
 --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
```

Despite the warning message listing a set of JVM parameters as arguments, not all of these are needed, probably due to
 either the integration tests not triggering specific functionality in Hazelcast, or the Payara5 container already
 including the parameters.  The following is the list of JVM parameters specified in the warning message that were
 found to NOT be necessary to include in the maven failsafe argLine configuration property:
* --add-modules java.se
* --add-exports java.base/jdk.internal.ref=ALL-UNNAMED
* --add-opens java.base/java.nio=ALL-UNNAMED
* --add-opens java.base/sun.nio.ch=ALL-UNNAMED
* --add-opens java.management/sun.management=ALL-UNNAMED
* --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED

The JVM parameter(s) from the Hazelcast warning message that ARE necessary:
* --add-opens java.base/java.lang=ALL-UNNAMED

Outside of the JVM parameters specified in the Hazelcast warning message, other JVM parameters were found to be
 necessary for the integration tests to run successfully.  These required JVM parameters where identified by inspecting
 the logging output of Payara5 during the arquillian bootstrapping/deployment, and consist of the following JVM
 parameters:
* --add-opens=java.base/sun.net.www=ALL-UNNAMED
* --add-opens=java.base/sun.security.util=ALL-UNNAMED

Even when the integration tests succeed, inspecting the logging output of Payara5 revealed warnings, errors, and
 exception messages that indicated JPMS related issues that can be mitigated with the following additional JVM
 parameters:
* --add-opens=java.base/java.net=ALL-UNNAMED
* --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED
* --add-opens=java.naming/javax.naming.spi=ALL-UNNAMED


 