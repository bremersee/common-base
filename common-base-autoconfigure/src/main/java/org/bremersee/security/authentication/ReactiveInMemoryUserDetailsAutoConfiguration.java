/*
 * Copyright 2020 the original author or authors.
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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ClassUtils;

/**
 * The reactive in memory user details auto configuration.
 *
 * @author Christian Bremer
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ReactiveAuthenticationManager.class})
@ConditionalOnMissingBean(
    value = {
        ReactiveAuthenticationManager.class,
        ReactiveUserDetailsService.class
    },
    type = {
        "org.springframework.security.oauth2.jwt.ReactiveJwtDecoder",
        "org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector"
    })
@Conditional(ReactiveInMemoryUserDetailsAutoConfiguration.ReactiveUserDetailsServiceCondition.class)
@AutoConfigureAfter(RSocketMessagingAutoConfiguration.class)
@AutoConfigureBefore(ReactiveUserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties({
    SecurityProperties.class,
    AuthProperties.class})
@Slf4j
public class ReactiveInMemoryUserDetailsAutoConfiguration {

  /**
   * Creates a reactive user details service bean.
   *
   * @param securityProperties the security properties
   * @param authProperties the auth properties
   * @param passwordEncoder the password encoder
   * @return the map reactive user details service
   */
  @Bean
  public MapReactiveUserDetailsService reactiveUserDetailsService(
      SecurityProperties securityProperties,
      AuthProperties authProperties,
      ObjectProvider<PasswordEncoder> passwordEncoder) {

    log.info("\n"
            + "*********************************************************************************\n"
            + "* {} is creating a {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        ClassUtils.getUserClass(MapReactiveUserDetailsService.class).getSimpleName());

    return Optional.of(authProperties.buildBasicAuthUserDetails(passwordEncoder.getIfAvailable()))
        .filter(userDetails -> userDetails.length > 0)
        .map(MapReactiveUserDetailsService::new)
        .orElseGet(() -> new ReactiveUserDetailsServiceAutoConfiguration()
            .reactiveUserDetailsService(securityProperties, passwordEncoder));
  }

  /**
   * The reactive user details service condition.
   */
  static class ReactiveUserDetailsServiceCondition extends AnyNestedCondition {

    /**
     * Instantiates a new Reactive user details service condition.
     */
    ReactiveUserDetailsServiceCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    /**
     * The type R socket security enabled condition.
     */
    @ConditionalOnBean(type = {
        "org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler"})
    @SuppressWarnings("unused")
    static class RSocketSecurityEnabledCondition {

    }

    /**
     * The type Reactive web application condition.
     */
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @SuppressWarnings("unused")
    static class ReactiveWebApplicationCondition {

    }
  }

}
