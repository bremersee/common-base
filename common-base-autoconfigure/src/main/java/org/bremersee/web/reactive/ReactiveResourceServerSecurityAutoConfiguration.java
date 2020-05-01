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

package org.bremersee.web.reactive;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.PathMatcherProperties;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.RoleOrIpBasedAuthorizationManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
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
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The reactive resource server security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "bremersee.security.authentication",
    name = "resource-server-auto-configuration",
    havingValue = "true")
@ConditionalOnClass({
    ServerHttpSecurity.class,
    SecurityWebFilterChain.class
})
@EnableConfigurationProperties(SecurityProperties.class)
@Configuration
@Slf4j
public class ReactiveResourceServerSecurityAutoConfiguration {

  private SecurityProperties securityProperties;

  private ObjectProvider<JsonPathReactiveJwtConverter> jwtConverterProvider;

  /**
   * Instantiates a new reactive resource server security auto configuration.
   *
   * @param securityProperties the security properties
   * @param jwtConverterProvider the jwt converter provider
   */
  public ReactiveResourceServerSecurityAutoConfiguration(
      SecurityProperties securityProperties,
      ObjectProvider<JsonPathReactiveJwtConverter> jwtConverterProvider) {
    this.securityProperties = securityProperties;
    this.jwtConverterProvider = jwtConverterProvider;
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
   * Resource server filter chain.
   *
   * @param httpProvider the http provider
   * @return the security web filter chain
   */
  @Bean
  @Order(51)
  public SecurityWebFilterChain resourceServerFilterChain(
      ObjectProvider<ServerHttpSecurity> httpProvider) {

    ServerHttpSecurity http = httpProvider.getIfAvailable();
    Assert.notNull(http, "Server http security must be present.");
    AuthorizeExchangeSpec spec = http
        .securityMatcher(new NegatedServerWebExchangeMatcher(EndpointRequest.toAnyEndpoint()))
        .authorizeExchange();
    spec = configurePathMatchers(spec);
    http = configureAuthenticationManager(spec.and());
    http = http.csrf().disable();
    if (!securityProperties.getCors().isEnable()) {
      http = http.cors().disable();
    }
    return http.build();
  }

  private AuthorizeExchangeSpec configurePathMatchers(AuthorizeExchangeSpec spec) {

    for (PathMatcherProperties props : securityProperties.getAuthentication().pathMatchers()) {
      log.info("Securing requests to {}", props);
      switch (props.getAccessMode()) {
        case DENY_ALL:
          spec = spec.matchers(matcher(props)).denyAll();
          break;
        case PERMIT_ALL:
          spec = spec.matchers(matcher(props)).permitAll();
          break;
        default:
          spec = spec.matchers(matcher(props)).access(new RoleOrIpBasedAuthorizationManager(
              props.roles(securityProperties.getAuthentication()::ensureRolePrefix),
              props.getIpAddresses()));
      }
    }
    return spec;
  }

  private ServerWebExchangeMatcher matcher(PathMatcherProperties props) {
    return Optional.ofNullable(props.httpMethod())
        .map(method -> ServerWebExchangeMatchers.pathMatchers(method, props.getAntPattern()))
        .orElseGet(() -> ServerWebExchangeMatchers.pathMatchers(props.getAntPattern()));
  }

  private ServerHttpSecurity configureAuthenticationManager(ServerHttpSecurity http) {

    if (securityProperties.getAuthentication().isEnableJwtSupport()) {
      log.info("Configure authentication provider with JWT.");
      JsonPathReactiveJwtConverter jwtConverter = jwtConverterProvider.getIfAvailable();
      Assert.notNull(jwtConverter, "JWT converter must be present.");
      return http
          .oauth2ResourceServer((rs) -> rs
              .jwt()
              .jwtAuthenticationConverter(jwtConverter)
              .and());
    }
    log.info("Configure authentication provider with basic auth and in-memory users.");
    return http
        .authenticationManager(inMemoryAuthenticationManager())
        .httpBasic()
        .and()
        .formLogin().disable();
  }

  private ReactiveAuthenticationManager inMemoryAuthenticationManager() {
    AuthenticationProperties props = securityProperties.getAuthentication();
    UserDetails[] userDetails = props.buildBasicAuthUserDetails();
    ReactiveUserDetailsService userDetailsService = new MapReactiveUserDetailsService(userDetails);
    return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
  }

}
