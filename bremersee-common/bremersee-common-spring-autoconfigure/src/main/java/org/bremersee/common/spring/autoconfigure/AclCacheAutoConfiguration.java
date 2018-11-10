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

import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass(name = {
        "org.springframework.security.acls.jdbc.JdbcMutableAclService",
        "org.bremersee.common.security.acls.domain.jpa.entity.AclClass"})
@EnableConfigurationProperties(AclProperties.class)
@EnableCaching
public class AclCacheAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AclCacheAutoConfiguration.class);

    @Autowired
    AclProperties aclProperties;

    @Autowired
    AuditLogger auditLogger;

    @Autowired(required = false)
    CacheManager cacheManager;

    @Autowired
    SidRetrievalStrategy sidRetrievalStrategy;

    @PostConstruct
    public void init() {
        // @formatter:off
        LOG.info("\n" // NOSONAR
                + "**********************************************************************\n"
                + "*  ACL Cache Auto Configuration                                      *\n"
                + "**********************************************************************\n"
                + "* - auditLogger = " + auditLogger + "\n"
                + "* - cacheManager = " + cacheManager + "\n"
                + "* - sidRetrievalStrategy= " + sidRetrievalStrategy + "\n"
                + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public EhCacheFactoryBean ehCacheFactoryBean() {
        final EhCacheFactoryBean factoryBean = new EhCacheFactoryBean();
        factoryBean.setName(StringUtils.isEmpty(
                aclProperties.getAclCacheName()) ? "aclCache" : aclProperties.getAclCacheName());
        //noinspection ConstantConditions
        if (cacheManager != null) {
            factoryBean.setCacheManager(cacheManager);
        }
        return factoryBean;
    }

    @Bean
    public EhCacheBasedAclCache aclCache() {
        return new EhCacheBasedAclCache(
                ehCacheFactoryBean().getObject(),
                permissionGrantingStrategy(),
                aclAuthorizationStrategy());
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(auditLogger);
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        final GrantedAuthority gaTakeOwnership = new SimpleGrantedAuthority(aclProperties.getTakeOwnershipRole());
        final GrantedAuthority gaModifyAuditing = new SimpleGrantedAuthority(aclProperties.getModifyAuditingRole());
        final GrantedAuthority gaGeneralChanges = new SimpleGrantedAuthority(aclProperties.getGeneralChangesRole());
        final AclAuthorizationStrategyImpl impl = new AclAuthorizationStrategyImpl(
                gaTakeOwnership,
                gaModifyAuditing,
                gaGeneralChanges
        );
        impl.setSidRetrievalStrategy(sidRetrievalStrategy);
        return impl;
    }

}
