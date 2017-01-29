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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.security.access.PermissionCacheOptimizer;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.authentication.AuthenticationTrustResolver;

import javax.annotation.PostConstruct;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnClass(name = {
        "org.springframework.security.acls.jdbc.JdbcMutableAclService",
        "org.bremersee.common.security.acls.domain.jpa.entity.AclClass"})
@EnableConfigurationProperties(AclProperties.class)
public class AclPermissionEvaluatorAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AclPermissionEvaluatorAutoConfiguration.class);

    @Autowired
    AclProperties aclProperties;

    @Autowired
    AclService aclService;

    @Autowired
    ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy;

    @Autowired
    ObjectIdentityGenerator objectIdentityGenerator;

    @Autowired
    SidRetrievalStrategy sidRetrievalStrategy;

    @Autowired
    PermissionFactory permissionFactory;

    @Autowired
    AuthenticationTrustResolver trustResolver;

    @Autowired
    ParameterNameDiscoverer parameterNameDiscoverer;

    @Autowired(required = false)
    PermissionCacheOptimizer permissionCacheOptimizer;

    @PostConstruct
    public void init() {
        // @formatter:off
        LOG.info("\n"
                + "**********************************************************************\n"
                + "*  ACL Permission Evaluator Auto Configuration                       *\n"
                + "**********************************************************************\n"
                + "* - aclService = " + aclService + "\n"
                + "* - objectIdentityRetrievalStrategy = " + objectIdentityRetrievalStrategy + "\n"
                + "* - objectIdentityGenerator = " + objectIdentityGenerator + "\n"
                + "* - sidRetrievalStrategy = " + sidRetrievalStrategy + "\n"
                + "* - permissionFactory = " + permissionFactory + "\n"
                + "* - trustResolver = " + trustResolver + "\n"
                + "* - parameterNameDiscoverer = " + parameterNameDiscoverer + "\n"
                + "* - permissionCacheOptimizer = " + permissionCacheOptimizer + "\n"
                + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        final AclPermissionEvaluator evaluator = new AclPermissionEvaluator(aclService);
        evaluator.setObjectIdentityGenerator(objectIdentityGenerator);
        evaluator.setObjectIdentityRetrievalStrategy(objectIdentityRetrievalStrategy);
        evaluator.setPermissionFactory(permissionFactory);
        evaluator.setSidRetrievalStrategy(sidRetrievalStrategy);
        return evaluator;
    }

    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler() {
        final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator());
        expressionHandler.setDefaultRolePrefix(aclProperties.getDefaultRolePrefix());
        expressionHandler.setParameterNameDiscoverer(parameterNameDiscoverer);
        expressionHandler.setTrustResolver(trustResolver);
        expressionHandler.setPermissionCacheOptimizer(permissionCacheOptimizer);
        return expressionHandler;
    }

}