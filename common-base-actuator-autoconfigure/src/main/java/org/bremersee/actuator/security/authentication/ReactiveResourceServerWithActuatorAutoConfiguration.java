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

package org.bremersee.actuator.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.authentication.AbstractReactiveResourceServerAutoConfiguration;
import org.bremersee.security.authentication.AuthProperties;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.PasswordFlowProperties;
import org.bremersee.security.authentication.ReactiveResourceServerAutoConfiguration;
import org.bremersee.web.CorsProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;

/**
 * The reactive resource server with actuator security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnExpression(
    "'${bremersee.auth.resource-server:OTHER}' != T(org.bremersee.security.authentication.AutoSecurityMode).OTHER")
@ConditionalOnClass({
    ServerHttpSecurity.class,
    ReactiveAuthenticationManager.class,
    PasswordFlowProperties.class,
    Info.class
})
@AutoConfigureBefore(ReactiveResourceServerAutoConfiguration.class)
@EnableConfigurationProperties({CorsProperties.class, AuthProperties.class})
@Configuration
@Slf4j
public class ReactiveResourceServerWithActuatorAutoConfiguration
    extends AbstractReactiveResourceServerAutoConfiguration {

  /**
   * Instantiates a new reactive resource server with actuator security auto configuration.
   *
   * @param securityProperties the security properties
   * @param jwtConverterProvider the jwt converter provider
   * @param userDetailsServiceProvider the user details service provider
   * @param passwordEncoderProvider the password encoder provider
   */
  public ReactiveResourceServerWithActuatorAutoConfiguration(
      CorsProperties corsProperties,
      AuthProperties securityProperties,
      ObjectProvider<JsonPathReactiveJwtConverter> jwtConverterProvider,
      ObjectProvider<ReactiveUserDetailsService> userDetailsServiceProvider,
      ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
    super(corsProperties, securityProperties, jwtConverterProvider, userDetailsServiceProvider,
        passwordEncoderProvider);
  }

  @EventListener(ApplicationReadyEvent.class)
  @Override
  public void init() {
    super.init();
  }

  @Override
  protected AuthorizeExchangeSpec init(ServerHttpSecurity http) {
    return http
        .securityMatcher(new NegatedServerWebExchangeMatcher(EndpointRequest.toAnyEndpoint()))
        .authorizeExchange();
  }

  /**
   * Resource server filter chain.
   *
   * @param httpProvider the http provider
   * @return the security web filter chain
   */
  @Bean
  public SecurityWebFilterChain resourceServerFilterChain(
      ObjectProvider<ServerHttpSecurity> httpProvider) {
    return super.resourceServerFilterChain(httpProvider.getIfAvailable());
  }

}
