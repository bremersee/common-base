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

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PruneStrategy;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.unboundid.UnboundIDProvider;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@AutoConfigureAfter(value = { SchedulingAutoConfiguration.class })
@ConditionalOnClass(name = { "org.ldaptive.DefaultConnectionFactory",
        "org.ldaptive.pool.PooledConnectionFactory" })
@EnableConfigurationProperties(LdaptiveProperties.class)
public class LdaptiveAutoConfiguration {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LdaptiveProperties properties;

    @Autowired(required = false)
    @Qualifier("inMemoryDirectoryServerBean")
    private Object inMemoryDirectoryServerBean;

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  LDAP Connection Factory Auto Configuration                        *\n"
               + "**********************************************************************\n"
               + "properties = " + properties + "\n"
               + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public ConnectionFactory connectionFactory() {

        if (inMemoryDirectoryServerBean != null) {
            return defaultConnectionFactory(new UnboundIDProvider());
        }

        if (properties.isPooled()) {
            return pooledConnectionFactory(null);
        }
        return defaultConnectionFactory(null);
    }

    private DefaultConnectionFactory defaultConnectionFactory(Provider<?> provider) {
        DefaultConnectionFactory factory = new DefaultConnectionFactory();
        factory.setConnectionConfig(connectionConfig());
        if (provider != null) {
            factory.setProvider(provider);
        }
        return factory;
    }

    private ConnectionConfig connectionConfig() {
        
        ConnectionConfig cc = new ConnectionConfig();
        cc.setLdapUrl(properties.getLdapUrl());
        
        //cc.setConnectTimeout(properties.getConnectTimeout());
        //cc.setResponseTimeout(properties.getResponseTimeout());
        if (properties.getConnectTimeout() > 0L) {
            cc.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeout()));
        }
        if (properties.getResponseTimeout() > 0L) {
            cc.setResponseTimeout(Duration.ofMillis(properties.getResponseTimeout()));
        }
        
        // default one
//        ConnectionStrategy strategy = new DefaultConnectionStrategy();
//        cc.setConnectionStrategy(strategy);

        cc.setUseSSL(properties.isUseSsl());
        cc.setUseStartTLS(properties.isUseStartTls());

        if (properties.isUseSsl() || properties.isUseStartTls()) {
            cc.setSslConfig(sslConfig());
        }

        // binds all operations to a dn
        if (StringUtils.isNotBlank(properties.getBindDn())) {
            cc.setConnectionInitializer(connectionInitializer());
        }

        return cc;
    }

    private SslConfig sslConfig() {
        SslConfig sc = new SslConfig();
        sc.setCredentialConfig(sslCredentialConfig());
        // there may be other ways
        // sc.setEnabledCipherSuites(suites);
        // sc.setEnabledProtocols(protocols);
        // sc.setHandshakeCompletedListeners(listeners);
        // sc.setTrustManagers(managers);
        return sc;
    }

    private CredentialConfig sslCredentialConfig() {
        // there may be other ways
        X509CredentialConfig x509 = new X509CredentialConfig();
        x509.setAuthenticationCertificate(properties.getAuthenticationCertificate());
        x509.setAuthenticationKey(properties.getAuthenticationKey());
        x509.setTrustCertificates(properties.getTrustCertificates());
        return x509;
    }

    private ConnectionInitializer connectionInitializer() {
        // sasl is not supported at the moment
        BindConnectionInitializer bci = new BindConnectionInitializer();
        bci.setBindDn(properties.getBindDn());
        bci.setBindCredential(new Credential(properties.getBindCredential()));
        return bci;
    }

    private PooledConnectionFactory pooledConnectionFactory(Provider<?> provider) {
        PooledConnectionFactory factory = new PooledConnectionFactory();
        factory.setConnectionPool(connectionPool(provider));
        return factory;
    }

    private ConnectionPool connectionPool(Provider<?> provider) {
        BlockingConnectionPool pool = new BlockingConnectionPool();
        pool.setConnectionFactory(defaultConnectionFactory(provider));
        pool.setPoolConfig(poolConfig());
        pool.setPruneStrategy(pruneStrategy());
        pool.setValidator(searchValidator());
        //pool.setBlockWaitTime(properties.getBlockWaitTime());
        if (properties.getBlockWaitTime() > 0L) {
            pool.setBlockWaitTime(Duration.ofMillis(properties.getBlockWaitTime()));
        }
        pool.initialize();
        return pool;
    }

    private PoolConfig poolConfig() {
        PoolConfig pc = new PoolConfig();
        pc.setMaxPoolSize(properties.getMaxPoolSize());
        pc.setMinPoolSize(properties.getMinPoolSize());
        pc.setValidateOnCheckIn(properties.isValidateOnCheckIn());
        pc.setValidateOnCheckOut(properties.isValidateOnCheckOut());
        //pc.setValidatePeriod(properties.getValidatePeriod());
        if (properties.getValidatePeriod() > 0L) {
            pc.setValidatePeriod(Duration.ofSeconds(properties.getValidatePeriod()));
        }
        pc.setValidatePeriodically(properties.isValidatePeriodically());
        return pc;
    }

    private PruneStrategy pruneStrategy() {
        // there may be other ways
        IdlePruneStrategy ips = new IdlePruneStrategy();
        //ips.setIdleTime(properties.getIdleTime());
        //ips.setPrunePeriod(properties.getPrunePeriod());
        if (properties.getIdleTime() > 0L) {
            ips.setIdleTime(Duration.ofSeconds(properties.getIdleTime()));
        }
        if (properties.getPrunePeriod() > 0L) {
            ips.setPrunePeriod(Duration.ofSeconds(properties.getPrunePeriod()));
        }
        return ips;
    }

    private SearchValidator searchValidator() {
        return properties.getSearchValidator();
    }

}
