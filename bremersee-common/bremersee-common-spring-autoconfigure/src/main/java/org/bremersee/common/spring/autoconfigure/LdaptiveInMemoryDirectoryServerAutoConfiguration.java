/*
 * Copyright 2015 the original author or authors.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "bremersee.unboundid.ldap.server", name = "embedded", havingValue = "true", matchIfMissing = false)
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

        try {
            final com.unboundid.ldap.listener.InMemoryDirectoryServerConfig config = new com.unboundid.ldap.listener.InMemoryDirectoryServerConfig(
                    properties.getRootDn());

            if (StringUtils.hasText(properties.getManagerDn())) {
                config.addAdditionalBindCredentials(properties.getManagerDn(), properties.getManagerPassword());
            }

            final InetAddress ldapAddress = StringUtils.hasText(properties.getLdapAddress())
                    ? InetAddress.getByName(properties.getLdapAddress()) : null;
            final InetAddress ldapsAddress = StringUtils.hasText(properties.getLdapsAddress())
                    ? InetAddress.getByName(properties.getLdapsAddress()) : null;

            KeyManager keyManager = null;
            TrustManager trustManager = null;

            if (StringUtils.hasText(properties.getKeyStoreLocation())) {

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
                final char[] password = StringUtils.hasText(properties.getKeyStorePassword())
                        ? properties.getKeyStorePassword().toCharArray() : null;
                keyManager = new KeyStoreKeyManager(location, password, properties.getKeyStoreFormat(),
                        properties.getKeyAlias());
            }

            if (StringUtils.hasText(properties.getTrustStoreLocation())) {

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
                final char[] password = StringUtils.hasText(properties.getTrustStorePassword())
                        ? properties.getTrustStorePassword().toCharArray() : null;
                trustManager = new TrustStoreTrustManager(location, password, properties.getTrustStoreFormat(), true);
            }

//            if (keyManager == null || trustManager == null) {
//                
//                final File keyStoreFile = File.createTempFile("ldapServerKeyStore", ".jks");
//                keyStoreFile.deleteOnExit();
//                try (final OutputStream outputStream = new FileOutputStream(keyStoreFile)) {
//                    IOUtils.copy(new ClassPathResource("/ldap-server/truststore.jks").getInputStream(), outputStream);
//                }
//                if (keyManager == null) {
//                    keyManager = new KeyStoreKeyManager(keyStoreFile, "changeit".toCharArray());
//                }
//                if (trustManager == null) {
//                    trustManager = new TrustStoreTrustManager(keyStoreFile);
//                }
//            }

            final SSLUtil serverSSLUtil = new SSLUtil(keyManager, trustManager);
            final SSLUtil clientSSLUtil = new SSLUtil(trustManager);

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

            config.setEnforceSingleStructuralObjectClass(false);
            config.setEnforceAttributeSyntaxCompliance(true);

            String[] schemaLocations = properties.getSchemaLocationsAsArray();
            if (schemaLocations.length > 0) {
                for (int i = 0; i < schemaLocations.length; i++) {
                    if (schemaLocations[i].toLowerCase().startsWith(CLASSPATH_PREFIX)) {
                        final File schemaFile = File.createTempFile("ldapServer", ".schema");
                        schemaFile.deleteOnExit();
                        try (final OutputStream outputStream = new FileOutputStream(schemaFile)) {
                            IOUtils.copy(new ClassPathResource(schemaLocations[i].substring(CLASSPATH_PREFIX.length()))
                                    .getInputStream(), outputStream);
                        }
                        schemaLocations[i] = schemaFile.getCanonicalPath();
                    }
                }
                final Schema s = Schema.mergeSchemas(Schema.getSchema(schemaLocations));
                config.setSchema(s);
            }

            InMemoryDirectoryServer directoryServer = new InMemoryDirectoryServer(config);

            String[] ldifLocations = properties.getLdifLocationsAsArray();
            if (ldifLocations.length > 0) {
                for (int i = 0; i < ldifLocations.length; i++) {
                    String location = ldifLocations[i];
                    if (location.toLowerCase().startsWith(CLASSPATH_PREFIX.substring(CLASSPATH_PREFIX.length()))) {
                        final File ldifFile = File.createTempFile("ldapServer", ".ldif");
                        ldifFile.deleteOnExit();
                        try (final OutputStream outputStream = new FileOutputStream(ldifFile)) {
                            IOUtils.copy(new ClassPathResource(location.substring(CLASSPATH_PREFIX.length()))
                                    .getInputStream(), outputStream);
                        }
                        location = ldifFile.getCanonicalPath();
                    }
                    directoryServer.importFromLDIF(true, location);
                }

                directoryServer.restartServer();

            } else {

                directoryServer.startListening();
            }

            return new InMemoryDirectoryServerBean(directoryServer);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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

}
