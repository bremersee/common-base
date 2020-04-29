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

package org.bremersee.actuator.web.reactive;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.actuator.security.ActuatorSecurityProperties;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.PasswordFlowReactiveAuthenticationManager;
import org.bremersee.security.authentication.RoleBasedAuthorizationManager;
import org.bremersee.security.authentication.RoleOrIpBasedAuthorizationManager;
import org.bremersee.security.authentication.WebClientAccessTokenRetriever;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest.EndpointServerWebExchangeMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * The reactive actuator security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "bremersee.actuator.security",
    name = "enable-auto-configuration",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnClass({
    ServerHttpSecurity.class,
    SecurityWebFilterChain.class
})
@EnableConfigurationProperties({
    SecurityProperties.class,
    ActuatorSecurityProperties.class})
@Configuration
@Slf4j
public class ReactiveActuatorSecurityAutoConfiguration {

  private SecurityProperties securityProperties;

  private ActuatorSecurityProperties actuatorSecurityProperties;

  private ObjectProvider<WebClientAccessTokenRetriever> tokenRetrieverProvider;

  /**
   * Instantiates a new reactive actuator security auto configuration.
   *
   * @param securityProperties the security properties
   * @param actuatorSecurityProperties the actuator security properties
   * @param tokenRetrieverProvider the token retriever provider
   */
  public ReactiveActuatorSecurityAutoConfiguration(
      SecurityProperties securityProperties,
      ActuatorSecurityProperties actuatorSecurityProperties,
      ObjectProvider<WebClientAccessTokenRetriever> tokenRetrieverProvider) {
    this.securityProperties = securityProperties;
    this.actuatorSecurityProperties = actuatorSecurityProperties;
    this.tokenRetrieverProvider = tokenRetrieverProvider;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Actuator filter chain security web filter chain.
   *
   * @param httpProvider the http provider
   * @return the security web filter chain
   */
  @Bean
  @Order(61)
  public SecurityWebFilterChain actuatorFilterChain(
      ObjectProvider<ServerHttpSecurity> httpProvider) {

    ServerHttpSecurity http = httpProvider.getIfAvailable();
    Assert.notNull(http, "Server http security must be present.");

    log.info("Authorizing requests to /actuator/**");
    return http
        .securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeExchange()
        .matchers(unauthenticatedEndpointMatchers()).permitAll()
        .matchers(new AndServerWebExchangeMatcher(
            EndpointRequest.toAnyEndpoint(),
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**")))
        .access(new RoleOrIpBasedAuthorizationManager(
            actuatorSecurityProperties.rolesOrDefaults(),
            actuatorSecurityProperties.getIpAddresses()))
        .anyExchange()
        .access(new RoleBasedAuthorizationManager(
            actuatorSecurityProperties.adminRolesOrDefaults()))
        .and()
        .authenticationManager(authenticationManager())
        .httpBasic()
        .and()
        .formLogin().disable()
        .csrf().disable()
        .build();
  }

  private EndpointServerWebExchangeMatcher[] unauthenticatedEndpointMatchers() {
    return actuatorSecurityProperties.getUnauthenticatedEndpoints().stream()
        .map(EndpointRequest::to)
        .toArray(EndpointServerWebExchangeMatcher[]::new);
  }

  private ReactiveAuthenticationManager authenticationManager() {
    if (actuatorSecurityProperties.isEnableJwtSupport()) {
      return passwordFlowAuthenticationManager();
    }
    return inMemoryAuthenticationManager();
  }

  @SuppressWarnings("DuplicatedCode")
  private ReactiveAuthenticationManager passwordFlowAuthenticationManager() {

    NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
        .withJwkSetUri(actuatorSecurityProperties.getJwkUriSet())
        .jwsAlgorithm(SignatureAlgorithm.from(actuatorSecurityProperties.getJwsAlgorithm()))
        .build();
    if (StringUtils.hasText(actuatorSecurityProperties.getIssuerUri())) {
      jwtDecoder.setJwtValidator(
          JwtValidators.createDefaultWithIssuer(actuatorSecurityProperties.getIssuerUri()));
    }
    JsonPathJwtConverter jwtConverter = new JsonPathJwtConverter();
    jwtConverter.setNameJsonPath(actuatorSecurityProperties.getNameJsonPath());
    jwtConverter.setRolePrefix(actuatorSecurityProperties.getRolePrefix());
    jwtConverter.setRolesJsonPath(actuatorSecurityProperties.getRolesJsonPath());
    jwtConverter.setRolesValueList(actuatorSecurityProperties.isRolesValueList());
    jwtConverter.setRolesValueSeparator(actuatorSecurityProperties.getRolesValueSeparator());
    return new PasswordFlowReactiveAuthenticationManager(
        actuatorSecurityProperties.getPasswordFlow(),
        jwtDecoder,
        new JsonPathReactiveJwtConverter(jwtConverter),
        tokenRetrieverProvider.getIfAvailable(WebClientAccessTokenRetriever::new));
  }

  private ReactiveAuthenticationManager inMemoryAuthenticationManager() {

    MapReactiveUserDetailsService userDetailsService = new MapReactiveUserDetailsService(
        securityProperties.getAuthentication().buildBasicAuthUserDetails());
    return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
  }

}
