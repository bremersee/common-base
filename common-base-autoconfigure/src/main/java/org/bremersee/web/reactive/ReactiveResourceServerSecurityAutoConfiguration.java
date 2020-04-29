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

import static org.bremersee.security.SecurityProperties.AuthenticationProperties.ApplicationAccessProperties.ALL_HTTP_METHODS;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.ApplicationAccessProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.ApplicationAccessProperties.PathMatcherProperties;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.RoleBasedAuthorizationManager;
import org.bremersee.security.authentication.RoleOrIpBasedAuthorizationManager;
import org.bremersee.security.core.AuthorityConstants;
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
    prefix = "bremersee.security",
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
    spec = configurePermitAll(spec);
    spec = configureAdminAccess(spec);
    spec = configureUserAccess(spec);
    http = configureAuthenticationManager(spec.and());
    http = http.csrf().disable();
    if (securityProperties.getCors().isDisabled()) {
      http = http.cors().disable();
    }
    return http.build();
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

  private AuthorizeExchangeSpec configureUserAccess(AuthorizeExchangeSpec spec) {

    log.info("Configure user access ...");
    ApplicationAccessProperties props = securityProperties.getAuthentication().getApplication();
    ServerWebExchangeMatcher[] matchers = matchers(props.getUserMatchers());
    Set<String> roles = props.userRolesOrDefaults(
        securityProperties.getAuthentication()::ensureRolePrefix);
    List<String> ipAddresses = props.getIpAddresses();
    log.info("Configure user access with roles {} and ip addresses {}.", roles, ipAddresses);
    RoleOrIpBasedAuthorizationManager manager = new RoleOrIpBasedAuthorizationManager(
        roles, ipAddresses);
    if (matchers.length > 0) {
      return spec.matchers(matchers).access(manager);
    }
    return spec.anyExchange().access(manager);
  }

  private AuthorizeExchangeSpec configureAdminAccess(AuthorizeExchangeSpec spec) {

    log.info("Configure admin access ...");
    ApplicationAccessProperties props = securityProperties.getAuthentication().getApplication();
    ServerWebExchangeMatcher[] matchers = matchers(props.getAdminMatchers());
    if (matchers.length > 0) {
      Set<String> roles = props.adminRolesOrDefaults(
          securityProperties.getAuthentication()::ensureRolePrefix,
          AuthorityConstants.ADMIN_ROLE_NAME);
      log.info("Configure admin access with roles {}.", roles);
      return spec.matchers(matchers).access(new RoleBasedAuthorizationManager(roles, false));
    } else {
      log.info("Configure admin access: Nothing to do because there are no path matchers.");
      return spec;
    }
  }

  private AuthorizeExchangeSpec configurePermitAll(AuthorizeExchangeSpec spec) {

    log.info("Configure permit all ...");
    ApplicationAccessProperties props = securityProperties.getAuthentication().getApplication();
    return Optional.of(matchers(props.getPermitAllMatchers()))
        .filter(matchers -> matchers.length > 0)
        .map(matchers -> spec.matchers(matchers).permitAll())
        .orElse(spec);
  }

  private ServerWebExchangeMatcher[] matchers(
      Collection<? extends PathMatcherProperties> properties) {

    return Optional.ofNullable(properties)
        .map(col -> col.stream()
            .sorted()
            .peek(props -> log.info("Using request matcher {}.", props))
            .map(props -> ALL_HTTP_METHODS.equals(props.getHttpMethod())
                ? ServerWebExchangeMatchers.pathMatchers(props.getAntPattern())
                : ServerWebExchangeMatchers
                    .pathMatchers(props.httpMethod(), props.getAntPattern()))
            .toArray(ServerWebExchangeMatcher[]::new))
        .orElseGet(() -> new ServerWebExchangeMatcher[0]);
  }

  private ReactiveAuthenticationManager inMemoryAuthenticationManager() {
    AuthenticationProperties props = securityProperties.getAuthentication();
    UserDetails[] userDetails = props.buildBasicAuthUserDetails();
    ReactiveUserDetailsService userDetailsService = new MapReactiveUserDetailsService(userDetails);
    return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
  }

}
