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

import org.bremersee.common.security.acls.jdbc.BasicLookupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass(name = {
        "org.springframework.security.acls.jdbc.JdbcMutableAclService",
        "org.bremersee.common.security.acls.domain.jpa.entity.AclClass"})
@EnableConfigurationProperties(AclProperties.class)
@EntityScan(basePackages = "org.bremersee.common.security.acls.domain.jpa.entity")
@ImportResource("classpath:acl-tx-advice.xml") // make the ACL service transaction
public class AclServiceAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AclServiceAutoConfiguration.class);

    @Autowired
    AclProperties aclProperties;

    @Autowired
    DataSource dataSource;

    @Autowired
    AuditLogger auditLogger;

    @Autowired
    AclCache aclCache;

    @Autowired
    AclAuthorizationStrategy aclAuthorizationStrategy;

    @PostConstruct
    public void init() {
        // @formatter:off
        LOG.info("\n"
                + "**********************************************************************\n"
                + "*  ACL Service Auto Configuration                                    *\n"
                + "**********************************************************************\n"
                + "* - dataSource = " + dataSource + "\n"
                + "* - aclCache = " + aclCache + "\n"
                + "* - aclAuthorizationStrategy = " + aclAuthorizationStrategy + "\n"
                + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public JdbcMutableAclService aclService() {
        final JdbcMutableAclService service = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache);
        service.setClassIdentityQuery(aclProperties.getClassIdentityQuery());
        service.setSidIdentityQuery(aclProperties.getSidIdentityQuery());
        return service;
    }

    @Bean
    public LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(dataSource, aclCache, aclAuthorizationStrategy, auditLogger);
    }

}