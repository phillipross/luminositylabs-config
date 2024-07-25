package org.jboss.arquillian.protocol.servlet5;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;

import java.net.URI;
import java.net.URISyntaxException;

public final class ServletUtil {
    public static final ArchivePath WEB_XML_PATH = ArchivePaths.create("WEB-INF/web.xml");
    public static final ArchivePath APPLICATION_XML_PATH = ArchivePaths.create("META-INF/application.xml");

    private ServletUtil() {
    }

    public static URI determineBaseURI(ServletProtocolConfiguration config, HTTPContext context, String servletName) {
        String scheme = config.getScheme();
        String host = config.getHost();
        Integer port = config.getPort();
        String contextRoot = null;
        Servlet servlet = context.getServletByName(servletName);
        if (servlet != null) {
            if (scheme == null) {
                scheme = "http";
            }

            if (host == null) {
                host = context.getHost();
            }

            if (port == null) {
                port = context.getPort();
            }

            contextRoot = servlet.getContextRoot();

            URI baseURI;
            try {
                var debug = Boolean.parseBoolean(System.getProperty(ServletUtil.class.getCanonicalName() + ".debug"));
                if (debug) {
                    System.err.println("E forming URI correctly with scheme[" + scheme + "], host[" + host + "], port[" + port + "], and contextRoot[" + contextRoot + "]");
                    System.err.println("E old URI would be " + URI.create(scheme + "://" + host + ":" + port + contextRoot));
                }
                baseURI = new URI(scheme, null, host, port,  contextRoot, null, null);
                if (debug) {
                    System.err.println("E current URI is " + baseURI);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException("Unable to form URI for deployment service, please contact DeployableContainer developer.", e);
            }

            return baseURI;
        } else {
            throw new IllegalArgumentException(servletName + " not found. Could not determine ContextRoot from ProtocolMetadata, please contact DeployableContainer developer.");
        }
    }

    public static String calculateContextRoot(String archiveName) {
        String correctedName = archiveName;
        if (correctedName.startsWith("/")) {
            correctedName = correctedName.substring(1);
        }

        if (correctedName.indexOf(".") != -1) {
            correctedName = correctedName.substring(0, correctedName.lastIndexOf("."));
        }

        return correctedName;
    }
}
