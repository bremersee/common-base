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

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.business.RoleNameServiceImpl;
import org.bremersee.common.security.crypto.password.PasswordEncoderSpringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

/**
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass(name = {
        "org.springframework.security.core.context.SecurityContextHolder"
})
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    SecurityProperties securityProperties;

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n" // NOSONAR
                + "**********************************************************************\n"
                + "*  Security Auto Configuration                                       *\n"
                + "**********************************************************************\n"
                + "securityProperties = " + securityProperties + "\n"
                + "**********************************************************************");
        // @formatter:on

        if (StringUtils.isNotBlank(securityProperties.getContextHolderStrategyName())) {
            SecurityContextHolder.setStrategyName(securityProperties.getContextHolderStrategyName());
        }
    }

    @Bean("roleNameService")
    public RoleNameService roleNameService() {
        return new RoleNameServiceImpl();
    }

    @Primary
    @Bean(name = {"passwordEncoder"})
    public PasswordEncoderSpringImpl passwordEncoder() {
        return new PasswordEncoderSpringImpl(securityProperties.getEncoder());
    }

}
