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

import org.bremersee.common.security.acls.model.CommonObjectIdentityRetrievalStrategy;
import org.bremersee.common.security.acls.model.ObjectIdentityRetrievalStrategyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.*;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.parameters.DefaultSecurityParameterNameDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass(name = {
        "org.springframework.security.acls.jdbc.JdbcMutableAclService",
        "org.bremersee.common.security.acls.domain.jpa.entity.AclClass"})
@EnableConfigurationProperties(AclProperties.class)
public class AclCommonAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AclCommonAutoConfiguration.class);

    @Autowired
    AclProperties aclProperties;

    @Autowired(required = false)
    protected List<ObjectIdentityRetrievalStrategyInterceptor> interceptors = new ArrayList<>();

    private CommonObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy;

    @PostConstruct
    public void init() {
        // @formatter:off
        LOG.info("\n" // NOSONAR
                + "**********************************************************************\n"
                + "*  ACL Common Auto Configuration                                     *\n"
                + "**********************************************************************\n"
                + "* - aclProperties = " + aclProperties + "\n"
                + "* - interceptors (size) = " + interceptors.size() + "\n"
                + "**********************************************************************");
        // @formatter:on
        if (objectIdentityRetrievalStrategy == null) {
            objectIdentityRetrievalStrategy = new CommonObjectIdentityRetrievalStrategy();
            objectIdentityRetrievalStrategy.setInterceptors(interceptors);
        }
    }

    @Bean
    @ConditionalOnMissingBean({ObjectIdentityRetrievalStrategy.class})
    public ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy() {
        LOG.info("Creating new 'ObjectIdentityRetrievalStrategy' ...");
        return objectIdentityRetrievalStrategy;
    }

    @Bean
    @ConditionalOnMissingBean({ObjectIdentityGenerator.class})
    public ObjectIdentityGenerator objectIdentityGenerator() {
        LOG.info("Creating new 'ObjectIdentityGenerator' ...");
        return objectIdentityRetrievalStrategy;
    }

    @Bean
    @ConditionalOnMissingBean({SidRetrievalStrategy.class})
    public SidRetrievalStrategy sidRetrievalStrategy() {
        SidRetrievalStrategyImpl impl = new SidRetrievalStrategyImpl();
        LOG.info("Creating new 'SidRetrievalStrategy' ...");
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean({PermissionFactory.class})
    public PermissionFactory permissionFactory() {
        DefaultPermissionFactory impl = new DefaultPermissionFactory();
        LOG.info("Creating new 'PermissionFactory' ...");
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean({AuthenticationTrustResolver.class})
    public AuthenticationTrustResolver trustResolver() {
        AuthenticationTrustResolverImpl impl = new AuthenticationTrustResolverImpl();
        LOG.info("Creating new 'AuthenticationTrustResolver' ...");
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean({ParameterNameDiscoverer.class})
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        DefaultSecurityParameterNameDiscoverer impl = new DefaultSecurityParameterNameDiscoverer();
        LOG.info("Creating new 'ParameterNameDiscoverer' ...");
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean({AuditLogger.class})
    public AuditLogger auditLogger() {
        return new Sl4jAuditLogger(aclProperties.getAuditLoggerName());
    }

    public class Sl4jAuditLogger implements AuditLogger {

        private final Logger log;

        public Sl4jAuditLogger(String loggerName) {
            log = LoggerFactory.getLogger(StringUtils.isEmpty(loggerName) ? "org.bremersee.acl.AuditLogger" : loggerName);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " (loggerName = " + log.getName() + ")";
        }

        @Override
        public void logIfNeeded(boolean granted, AccessControlEntry ace) {
            Assert.notNull(ace, "AccessControlEntry required");

            if (ace instanceof AuditableAccessControlEntry) {
                AuditableAccessControlEntry auditableAce = (AuditableAccessControlEntry) ace;

                if (granted && auditableAce.isAuditSuccess()) {
                    log.info("GRANTED due to ACE: {}", ace);
                } else if (!granted && auditableAce.isAuditFailure()) {
                    log.info("DENIED due to ACE: {}", ace);
                }
            }

        }
    }

}
