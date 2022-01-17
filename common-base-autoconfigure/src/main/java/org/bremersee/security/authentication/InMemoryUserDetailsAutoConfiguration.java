/*
 * Copyright 2020-2022 the original author or authors.
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

package org.bremersee.security.authentication;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.ClassUtils;

/**
 * The in memory user details autoconfiguration.
 *
 * @author Christan Bremer
 */
@AutoConfigureBefore(UserDetailsServiceAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(AuthenticationManager.class)
@ConditionalOnBean(ObjectPostProcessor.class)
@ConditionalOnMissingBean(
    value = {AuthenticationManager.class, AuthenticationProvider.class, UserDetailsService.class},
    type = {
        "org.springframework.security.oauth2.jwt.JwtDecoder",
        "org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector"
    })
@EnableConfigurationProperties({
    SecurityProperties.class,
    AuthProperties.class
})
@Slf4j
public class InMemoryUserDetailsAutoConfiguration {

  /**
   * Creates an in memory user details manager bean.
   *
   * @param securityProperties the security properties
   * @param authProperties the auth properties
   * @param passwordEncoder the password encoder
   * @return the in memory user details manager
   */
  @Bean
  @ConditionalOnMissingBean(
      type = "org.springframework.security.oauth2.client.registration.ClientRegistrationRepository")
  @Lazy
  public InMemoryUserDetailsManager inMemoryUserDetailsManager(
      SecurityProperties securityProperties,
      AuthProperties authProperties,
      ObjectProvider<PasswordEncoder> passwordEncoder) {

    log.info("\n"
            + "*********************************************************************************\n"
            + "* {} is creating a {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        ClassUtils.getUserClass(InMemoryUserDetailsManager.class).getSimpleName());

    return Optional.of(authProperties.buildBasicAuthUserDetails(passwordEncoder.getIfAvailable()))
        .filter(userDetails -> userDetails.length > 0)
        .map(InMemoryUserDetailsManager::new)
        .orElseGet(() -> new UserDetailsServiceAutoConfiguration()
            .inMemoryUserDetailsManager(securityProperties, passwordEncoder));
  }

}
