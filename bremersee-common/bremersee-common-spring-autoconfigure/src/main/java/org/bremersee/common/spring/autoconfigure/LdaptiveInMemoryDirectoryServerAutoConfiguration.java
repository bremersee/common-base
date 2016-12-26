/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.common.spring.autoconfigure;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "bremersee.unboundid.ldap.server",
        name = "embedded",
        havingValue = "true")
@ConditionalOnClass(name = { 
        "org.ldaptive.DefaultConnectionFactory",
        "org.ldaptive.pool.PooledConnectionFactory",
        "com.unboundid.ldap.listener.InMemoryDirectoryServer"
})
@EnableConfigurationProperties(LdaptiveInMemoryDirectoryServerProperties.class)
public class LdaptiveInMemoryDirectoryServerAutoConfiguration {

    private static final String CLASSPATH_PREFIX = "classpath:";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected LdaptiveInMemoryDirectoryServerProperties properties;

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  In Memory LDAP Server Auto Configuration                          *\n"
               + "**********************************************************************\n"
               + "properties = " + properties + "\n"
               + "**********************************************************************");
        // @formatter:on
    }

    @Bean(name = "inMemoryDirectoryServerBean", destroyMethod = "shutdown")
    public InMemoryDirectoryServerBean inMemoryDirectoryServerBean() {

        final com.unboundid.ldap.listener.InMemoryDirectoryServerConfig config;
        try {
            config = new com.unboundid.ldap.listener.InMemoryDirectoryServerConfig(
                    properties.getRootDn());

        } catch (LDAPException e) {
            final String message = "Creating new InMemoryDirectoryServerConfig failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }

        addAdditionalBindCredentials(config);
        setListenerConfigs(config);
        config.setEnforceSingleStructuralObjectClass(false);
        config.setEnforceAttributeSyntaxCompliance(true);
        setSchema(config);

        final InMemoryDirectoryServer directoryServer;
        try {
            directoryServer = new InMemoryDirectoryServer(config);

            String[] ldifLocations = getLdifLocations();
            if (ldifLocations.length > 0) {
                for (int i = 0; i < ldifLocations.length; i++) {
                    if (i == 0) {// NOSONAR
                        directoryServer.importFromLDIF(true, ldifLocations[i]);
                    } else {
                        directoryServer.importFromLDIF(false, ldifLocations[i]);
                    }
                }
                directoryServer.restartServer();
            } else {
                directoryServer.startListening();
            }

        } catch (LDAPException e) {
            final String message = "Creating new InMemoryDirectoryServer failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }

        return new InMemoryDirectoryServerBean(directoryServer);
    }

    private String getKeyStoreLocation() {
        try {
            String location = properties.getKeyStoreLocation();
            if (location.toLowerCase().startsWith(CLASSPATH_PREFIX)) {
                final File keyStoreFile = File.createTempFile("ldapServerKey", "Store");
                keyStoreFile.deleteOnExit();
                try (final OutputStream outputStream = new FileOutputStream(keyStoreFile)) {
                    IOUtils.copy(
                            new ClassPathResource(location.substring(CLASSPATH_PREFIX.length())).getInputStream(),
                            outputStream);
                }
                location = keyStoreFile.getCanonicalPath();
            }
            return location;

        } catch (IOException e) {
            final String message = "Creating temporary key store failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }
    }

    private KeyManager findKeyManager() {
        KeyManager keyManager = null;
        if (StringUtils.hasText(properties.getKeyStoreLocation())) {
            final String location = getKeyStoreLocation();
            final char[] password = StringUtils.hasText(properties.getKeyStorePassword())
                    ? properties.getKeyStorePassword().toCharArray() : null;
            try {
                keyManager = new KeyStoreKeyManager(location, password, properties.getKeyStoreFormat(),
                        properties.getKeyAlias());

            } catch (KeyStoreException e) {
                final String message = "Creating key manager failed.";
                log.error(message, e);
                throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
            }
        }
        return keyManager;
    }

    private String getTrustStoreLocation() {
        try {
            String location = properties.getTrustStoreLocation();
            if (location.toLowerCase().startsWith(CLASSPATH_PREFIX)) {
                final File trustStoreFile = File.createTempFile("ldapServerTrust", "Store");
                trustStoreFile.deleteOnExit();
                try (final OutputStream outputStream = new FileOutputStream(trustStoreFile)) {
                    IOUtils.copy(
                            new ClassPathResource(location.substring(CLASSPATH_PREFIX.length())).getInputStream(),
                            outputStream);
                }
                location = trustStoreFile.getCanonicalPath();
            }
            return location;

        } catch (IOException e) {
            final String message = "Creating temporary trust store failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }
    }

    private TrustManager findTrustManager() {
        TrustManager trustManager = null;
        if (StringUtils.hasText(properties.getTrustStoreLocation())) {
            final String location = getTrustStoreLocation();
            final char[] password = StringUtils.hasText(properties.getTrustStorePassword())
                    ? properties.getTrustStorePassword().toCharArray() : null;
            trustManager = new TrustStoreTrustManager(location, password, properties.getTrustStoreFormat(), true);
        }
        return trustManager;
    }

    private InetAddress getLdapInetAddress() {
        final InetAddress ldapAddress;
        try {
            ldapAddress = StringUtils.hasText(properties.getLdapAddress())
                    ? InetAddress.getByName(properties.getLdapAddress()) : null;
        } catch (UnknownHostException e) {
            final String message = "Getting ldap inet address failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }
        return ldapAddress;
    }

    private InetAddress getLdapsInetAddress() {
        final InetAddress ldapsAddress;
        try {
            ldapsAddress = StringUtils.hasText(properties.getLdapsAddress())
                    ? InetAddress.getByName(properties.getLdapsAddress()) : null;
        } catch (UnknownHostException e) {
            final String message = "Getting ldaps inet address failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }
        return ldapsAddress;
    }

    private void addAdditionalBindCredentials(final com.unboundid.ldap.listener.InMemoryDirectoryServerConfig config) {
        if (StringUtils.hasText(properties.getManagerDn())) {
            try {
                config.addAdditionalBindCredentials(properties.getManagerDn(), properties.getManagerPassword());
            } catch (LDAPException e) {
                final String message = "Adding additional bind credentials failed.";
                log.error(message, e);
                throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
            }
        }
    }

    private void setListenerConfigs(final com.unboundid.ldap.listener.InMemoryDirectoryServerConfig config) {
        final InetAddress ldapAddress = getLdapInetAddress();
        final InetAddress ldapsAddress = getLdapsInetAddress();
        final KeyManager keyManager = findKeyManager();
        final TrustManager trustManager = findTrustManager();
        final SSLUtil serverSSLUtil = new SSLUtil(keyManager, trustManager);
        final SSLUtil clientSSLUtil = new SSLUtil(trustManager);
        try {
            //@formatter:off
            config.setListenerConfigs(
                    InMemoryListenerConfig.createLDAPConfig(properties.getLdapListenerName(), // Listener name
                            ldapAddress, // Listen address. (null = listen on all interfaces)
                            properties.getLdapPort(), // Listen port (0 = automatically choose an available port)
                            serverSSLUtil.createSSLSocketFactory()), // StartTLS factory
                    InMemoryListenerConfig.createLDAPSConfig(properties.getLdapsListenerName(), // Listener name
                            ldapsAddress, // Listen address. (null = listen on all interfaces)
                            properties.getLdapsPort(), // Listen port (0 = automatically choose an available port)
                            serverSSLUtil.createSSLServerSocketFactory(), // Server factory
                            clientSSLUtil.createSSLSocketFactory())); // Client factory
            //@formatter:on

        } catch (GeneralSecurityException | LDAPException e) {
            final String message = "Setting listener configs failed.";
            log.error(message, e);
            throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
        }
    }

    private String[] getSchemaLocations() {
        final String[] schemaLocations = properties.getSchemaLocationsAsArray();
        if (schemaLocations.length > 0) {
            try {
                for (int i = 0; i < schemaLocations.length; i++) {
                    if (schemaLocations[i].toLowerCase().startsWith(CLASSPATH_PREFIX)) { // NOSONAR
                        final File schemaFile = File.createTempFile("ldapServer", ".schema");
                        schemaFile.deleteOnExit();
                        try (final OutputStream outputStream = new FileOutputStream(schemaFile)) {
                            IOUtils.copy(new ClassPathResource(schemaLocations[i].substring(CLASSPATH_PREFIX.length()))
                                    .getInputStream(), outputStream);
                        }
                        schemaLocations[i] = schemaFile.getCanonicalPath();
                    }
                }

            } catch (IOException e) {
                final String message = "Getting schema locations failed.";
                log.error(message, e);
                throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
            }
        }
        return schemaLocations;
    }

    private void setSchema(final com.unboundid.ldap.listener.InMemoryDirectoryServerConfig config) {
        final String[] schemaLocations = getSchemaLocations();
        if (schemaLocations.length > 0) {
            try {
                final Schema s = Schema.mergeSchemas(Schema.getSchema(schemaLocations));
                config.setSchema(s);

            } catch (IOException | LDIFException e) {
                final String message = "Setting schema failed.";
                log.error(message, e);
                throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
            }
        }
    }

    private String[] getLdifLocations() {
        final String[] ldifLocations = properties.getLdifLocationsAsArray();
        if (ldifLocations.length > 0) {
            try {
                for (int i = 0; i < ldifLocations.length; i++) {
                    String location = ldifLocations[i];
                    if (location.toLowerCase().startsWith(CLASSPATH_PREFIX)) { // NOSONAR
                        final File ldifFile = File.createTempFile("ldapServer", ".ldif");
                        ldifFile.deleteOnExit();
                        try (final OutputStream outputStream = new FileOutputStream(ldifFile)) {
                            IOUtils.copy(new ClassPathResource(location.substring(CLASSPATH_PREFIX.length()))
                                    .getInputStream(), outputStream);
                        }
                        ldifLocations[i] = ldifFile.getCanonicalPath();
                    }
                }

            } catch (IOException e) {
                final String message = "Getting ldif locations failed.";
                log.error(message, e);
                throw new LdaptiveInMemoryDirectoryServerConfigurationException(message, e);
            }
        }
        return ldifLocations;
    }

    public static class InMemoryDirectoryServerBean {

        private final InMemoryDirectoryServer server;

        private InMemoryDirectoryServerBean(InMemoryDirectoryServer server) {
            this.server = server;
        }

        public InMemoryDirectoryServer getServer() {
            return server;
        }

        public void shutdown() {
            if (server != null) {
                server.shutDown(true);
            }
        }
    }

    public static class LdaptiveInMemoryDirectoryServerConfigurationException extends RuntimeException {

        public LdaptiveInMemoryDirectoryServerConfigurationException(String message, Exception e) {
            super(message, e);
        }

    }

}
