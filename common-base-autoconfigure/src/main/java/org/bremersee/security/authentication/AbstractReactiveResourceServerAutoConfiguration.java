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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.core.OrderedProxy;
import org.bremersee.security.authentication.AuthProperties.PathMatcherProperties;
import org.bremersee.web.CorsProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The abstract reactive resource server security auto configuration.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class AbstractReactiveResourceServerAutoConfiguration {

  private final CorsProperties corsProperties;

  private final AuthProperties authProperties;

  private final ObjectProvider<JsonPathReactiveJwtConverter> jwtConverterProvider;

  private final ObjectProvider<ReactiveUserDetailsService> userDetailsServiceProvider;

  private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

  /**
   * Instantiates a new abstract reactive resource server security auto configuration.
   *
   * @param corsProperties the cors properties
   * @param authProperties the authentication nad authorization properties
   * @param jwtConverterProvider the jwt converter provider
   * @param userDetailsServiceProvider the user details service provider
   * @param passwordEncoderProvider the password encoder provider
   */
  protected AbstractReactiveResourceServerAutoConfiguration(
      CorsProperties corsProperties,
      AuthProperties authProperties,
      ObjectProvider<JsonPathReactiveJwtConverter> jwtConverterProvider,
      ObjectProvider<ReactiveUserDetailsService> userDetailsServiceProvider,
      ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
    this.corsProperties = corsProperties;
    this.authProperties = authProperties;
    this.jwtConverterProvider = jwtConverterProvider;
    this.userDetailsServiceProvider = userDetailsServiceProvider;
    this.passwordEncoderProvider = passwordEncoderProvider;
  }

  /**
   * Init.
   */
  protected void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Init authorize exchange.
   *
   * @param http the http
   * @return the authorize exchange spec
   */
  protected abstract AuthorizeExchangeSpec init(ServerHttpSecurity http);

  /**
   * Resource server filter chain.
   *
   * @param serverHttpSecurity the server http security
   * @return the security web filter chain
   */
  protected SecurityWebFilterChain resourceServerFilterChain(
      ServerHttpSecurity serverHttpSecurity) {

    ServerHttpSecurity http = serverHttpSecurity;
    Assert.notNull(http, "Server http security must be present.");
    AuthorizeExchangeSpec spec = init(http);
    if (authProperties.getResourceServer() == AutoSecurityMode.NONE) {
      http = spec
          .anyExchange().permitAll()
          .and()
          .httpBasic().disable();
    } else {
      spec = configurePathMatchers(spec);
      http = configureAuthenticationManager(spec.and());
    }
    http = http.csrf().disable();
    if (!corsProperties.isEnable()) {
      http = http.cors().disable();
    }
    return OrderedProxy.create(
        http.build(),
        authProperties.getResourceServerOrder());
  }

  @SuppressWarnings("DuplicatedCode")
  private AuthorizeExchangeSpec configurePathMatchers(AuthorizeExchangeSpec spec) {

    List<PathMatcherProperties> pathMatchers = new ArrayList<>(authProperties.getPathMatchers());
    PathMatcherProperties corsMatcher = new PathMatcherProperties();
    corsMatcher.setHttpMethod(HttpMethod.OPTIONS.name());
    corsMatcher.setAccessMode(AccessMode.PERMIT_ALL);
    if (corsProperties.isEnable() && !pathMatchers.contains(corsMatcher)) {
      pathMatchers.add(0, corsMatcher);
    }
    PathMatcherProperties anyRequestMatcher = new PathMatcherProperties();
    anyRequestMatcher.setAccessMode(authProperties.getAnyAccessMode());
    if (!pathMatchers.contains(anyRequestMatcher)) {
      pathMatchers.add(anyRequestMatcher);
    }
    for (PathMatcherProperties props : pathMatchers) {
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
              props.roles(authProperties::ensureRolePrefix),
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

    return Optional.ofNullable(jwtConverterProvider.getIfAvailable())
        .map(jwtConverter -> http
            .oauth2ResourceServer((rs) -> rs
                .jwt()
                .jwtAuthenticationConverter(jwtConverter)
                .and()))
        .orElseGet(() -> http
            .authenticationManager(userDetailsAuthenticationManager())
            .httpBasic()
            .and()
            .formLogin().disable());
  }

  private ReactiveAuthenticationManager userDetailsAuthenticationManager() {

    return Optional.ofNullable(userDetailsServiceProvider.getIfAvailable())
        .map(UserDetailsRepositoryReactiveAuthenticationManager::new)
        .orElseGet(() -> new UserDetailsRepositoryReactiveAuthenticationManager(
            new MapReactiveUserDetailsService(authProperties
                .buildBasicAuthUserDetails(passwordEncoderProvider.getIfAvailable()))));
  }

}