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

package org.bremersee.base.webflux.app;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.PasswordFlowReactiveAuthenticationManager;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.util.Assert;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {TestConfiguration.class})
@Slf4j
public class TestConfiguration {

  private JsonPathReactiveJwtConverter jwtConverter;

  private PasswordFlowReactiveAuthenticationManager passwordFlowAuthenticationManager;

  public TestConfiguration(
      ObjectProvider<JsonPathReactiveJwtConverter> jwtConverter,
      ObjectProvider<PasswordFlowReactiveAuthenticationManager> passwordFlowAuthenticationManager) {
    Assert.notNull(
        jwtConverter.getIfAvailable(),
        "JsonPathReactiveJwtConverter must be present.");
    Assert.notNull(
        passwordFlowAuthenticationManager.getIfAvailable(),
        "PasswordFlowReactiveAuthenticationManager must be present.");
    this.jwtConverter = jwtConverter.getIfAvailable();
    this.passwordFlowAuthenticationManager = passwordFlowAuthenticationManager.getIfAvailable();
  }

  /**
   * Builds the OAuth2 resource server filter chain.
   *
   * @param httpProvider the provider of http security configuration object
   * @return the security web filter chain
   */
  @Bean
  @Order(51)
  public SecurityWebFilterChain oauth2ResourceServerFilterChain(
      ObjectProvider<ServerHttpSecurity> httpProvider) {

    log.info("msg=[Creating resource server filter chain.]");
    ServerHttpSecurity http = httpProvider.getIfAvailable();
    Assert.notNull(http, "ServerHttpSecurity must be present.");
    http
        .securityMatcher(new NegatedServerWebExchangeMatcher(EndpointRequest.toAnyEndpoint()))
        .csrf().disable()
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(jwtConverter);

    http
        .authorizeExchange()
        .pathMatchers("/api/admin/**")
        .hasAuthority(AuthorityConstants.ADMIN_ROLE_NAME)
        .anyExchange()
        .authenticated();

    return http.build();
  }

  /**
   * Builds the actuator filter chain.
   *
   * @param httpProvider the provider of http security configuration object
   * @return the security web filter chain
   */
  @Bean
  @Order(52)
  public SecurityWebFilterChain actuatorFilterChain(
      ObjectProvider<ServerHttpSecurity> httpProvider) {

    log.info("msg=[Creating actuator filter chain.]");
    ServerHttpSecurity http = httpProvider.getIfAvailable();
    Assert.notNull(http, "ServerHttpSecurity must be present.");
    http
        .securityMatcher(EndpointRequest.toAnyEndpoint())
        .csrf().disable()
        .httpBasic()
        .authenticationManager(passwordFlowAuthenticationManager);

    http
        .authorizeExchange()
        .matchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
        // .matchers(EndpointRequest.to(InfoEndpoint.class)).permitAll()
        .anyExchange().hasAuthority(AuthorityConstants.ACTUATOR_ROLE_NAME);

    return http.build();
  }
}
