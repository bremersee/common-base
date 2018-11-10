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

    @Autowired(required = false)
    MethodSecurityExpressionHandler methodSecurityExpressionHandler;

    @PostConstruct
    public void init() {
        // @formatter:off
        LOG.info("\n" // NOSONAR
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
                + "* - methodSecurityExpressionHandler = " + methodSecurityExpressionHandler + "\n"
                + "**********************************************************************");
        // @formatter:on
        if (methodSecurityExpressionHandler instanceof DefaultMethodSecurityExpressionHandler) {
            DefaultMethodSecurityExpressionHandler expressionHandler = (DefaultMethodSecurityExpressionHandler)
                    methodSecurityExpressionHandler;
            expressionHandler.setPermissionEvaluator(permissionEvaluator());
            expressionHandler.setDefaultRolePrefix(aclProperties.getDefaultRolePrefix());
            expressionHandler.setParameterNameDiscoverer(parameterNameDiscoverer);
            expressionHandler.setTrustResolver(trustResolver);
            expressionHandler.setPermissionCacheOptimizer(permissionCacheOptimizer);
        } else {
            StringBuilder logMsgBuilder = new StringBuilder();
            logMsgBuilder.append("######################################################################\n");
            //noinspection ConstantConditions
            if (methodSecurityExpressionHandler == null) {
                logMsgBuilder.append("# The Method Security Expression Handler is NULL.                    #\n");
            } else {
                logMsgBuilder.append("# The Method Security Expression Handler is NOT an instance of       #\n");
                logMsgBuilder.append("# DefaultMethodSecurityExpressionHandler.                            #\n");
            }
            logMsgBuilder.append("#                                                                    #\n"); // NOSONAR
            logMsgBuilder.append("# The beans of the ACL configuration cannot be applied to it.        #\n");
            logMsgBuilder.append("# Security method expressions like                                   #\n");
            logMsgBuilder.append("# hasPermission(#name, 'Role', 'write') won't work.                  #\n");
            logMsgBuilder.append("#                                                                    #\n");
            logMsgBuilder.append("# Please provide a configuration like this:                          #\n");
            logMsgBuilder.append("#                                                                    #\n");
            logMsgBuilder.append("# @Configuration                                                     #\n");
            logMsgBuilder.append("# @EnableGlobalMethodSecurity(prePostEnabled = true, ...)            #\n");
            logMsgBuilder.append("# public class MethodSecurityConfig                                  #\n");
            logMsgBuilder.append("#              extends GlobalMethodSecurityConfiguration {           #\n");
            logMsgBuilder.append("#                                                                    #\n");
            logMsgBuilder.append("#     final DefaultMethodSecurityExpressionHandler handler =         #\n");
            logMsgBuilder.append("#         new DefaultMethodSecurityExpressionHandler();              #\n");
            logMsgBuilder.append("#         // or new OAuth2MethodSecurityExpressionHandler()          #\n");
            logMsgBuilder.append("#         // if you want to use security annotation like this:       #\n");
            logMsgBuilder.append("#         // @PreAuthorize(\"#oauth2.hasScope('requiredScope')\")      #\n");
            logMsgBuilder.append("#                                                                    #\n");
            logMsgBuilder.append("#     @Override                                                      #\n");
            logMsgBuilder.append("#     MethodSecurityExpressionHandler createExpressionHandler() {    #\n");
            logMsgBuilder.append("#         return handler();                                          #\n");
            logMsgBuilder.append("#     }                                                              #\n");
            logMsgBuilder.append("#                                                                    #\n");
            logMsgBuilder.append("#     @Bean(\"methodSecurityExpressionHandler\")                     #\n");
            logMsgBuilder.append("#     MethodSecurityExpressionHandler handler() {                    #\n");
            logMsgBuilder.append("#         return handler;                                            #\n");
            logMsgBuilder.append("#     }                                                              #\n");
            logMsgBuilder.append("# }                                                                  #\n");
            logMsgBuilder.append("#                                                                    #\n");
            logMsgBuilder.append("######################################################################");

            if (LOG.isWarnEnabled()) {
                LOG.warn(logMsgBuilder.toString());
            }
        }
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

//    @Bean                                                                                  // NOSONAR
//    public MethodSecurityExpressionHandler createExpressionHandler() {                     // NOSONAR
//        final DefaultMethodSecurityExpressionHandler expressionHandler =                   // NOSONAR
//                new DefaultMethodSecurityExpressionHandler();                              // NOSONAR
//        expressionHandler.setPermissionEvaluator(permissionEvaluator());                   // NOSONAR
//        expressionHandler.setDefaultRolePrefix(aclProperties.getDefaultRolePrefix());      // NOSONAR
//        expressionHandler.setParameterNameDiscoverer(parameterNameDiscoverer);             // NOSONAR
//        expressionHandler.setTrustResolver(trustResolver);                                 // NOSONAR
//        expressionHandler.setPermissionCacheOptimizer(permissionCacheOptimizer);           // NOSONAR
//        return expressionHandler;                                                          // NOSONAR
//    }                                                                                      // NOSONAR

}