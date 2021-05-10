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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.authentication.AuthProperties.PathMatcherProperties;
import org.bremersee.web.CorsProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * The abstract resource server security auto configuration.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class AbstractResourceServerAutoConfiguration extends WebSecurityConfigurerAdapter
    implements Ordered {

  @Getter(AccessLevel.PROTECTED)
  private final Environment environment;

  @Getter(AccessLevel.PROTECTED)
  private final SecurityProperties securityProperties;

  @Getter(AccessLevel.PROTECTED)
  private final AuthProperties authProperties;

  @Getter(AccessLevel.PROTECTED)
  private final CorsProperties corsProperties;

  @Getter(AccessLevel.PROTECTED)
  private final ObjectProvider<JsonPathJwtConverter> jwtConverterProvider;

  @Getter(AccessLevel.PROTECTED)
  private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

  /**
   * Instantiates a new abstract resource server security auto configuration.
   *
   * @param environment the environment
   * @param securityProperties the spring properties
   * @param authProperties the authentication and authorization properties
   * @param corsProperties the cors properties
   * @param jwtConverterProvider the jwt converter provider
   * @param passwordEncoderProvider the password encoder provider
   */
  protected AbstractResourceServerAutoConfiguration(
      Environment environment,
      SecurityProperties securityProperties,
      AuthProperties authProperties,
      CorsProperties corsProperties,
      ObjectProvider<JsonPathJwtConverter> jwtConverterProvider,
      ObjectProvider<PasswordEncoder> passwordEncoderProvider) {

    this.environment = environment;
    this.securityProperties = securityProperties;
    this.authProperties = authProperties;
    this.corsProperties = corsProperties;
    this.jwtConverterProvider = jwtConverterProvider;
    this.passwordEncoderProvider = passwordEncoderProvider;
  }

  @Override
  public int getOrder() {
    return authProperties.getResourceServerOrder();
  }

  /**
   * Init.
   */
  protected void init() {
    final boolean hasJwkUriSet = StringUtils
        .hasText(environment.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri"));
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* enable = {}\n"
            + "* order = {}\n"
            + "* jwt = {}\n"
            + "* cors = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        authProperties.getResourceServer().name(),
        authProperties.getResourceServerOrder(),
        hasJwkUriSet,
        corsProperties.isEnable());
  }

  /**
   * Init expression url authorization configurer . expression intercept url registry.
   *
   * @param httpSecurity the http security
   * @return the expression url authorization configurer . expression intercept url registry
   * @throws Exception the exception
   */
  protected abstract ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  init(HttpSecurity httpSecurity) throws Exception;

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {

    HttpSecurity http = httpSecurity;
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg = init(http);
    if (authProperties.getResourceServer() == AutoSecurityMode.NONE) {
      http = reg
          .anyRequest().permitAll()
          .and()
          .httpBasic().disable();
    } else {
      reg = configurePathMatchers(reg);
      http = configureAuthenticationProvider(reg.and());
    }
    http = http
        .headers().frameOptions(customizer -> {
          switch (authProperties.getFrameOptionsMode()) {
            case DISABLE: {
              customizer.disable();
              break;
            }
            case SAMEORIGIN: {
              customizer.sameOrigin();
            }
            default:
              customizer.deny();
          }
        })
        .and()
        .csrf().disable();
    if (corsProperties.isEnable()) {
      http.cors();
    } else {
      http.cors().disable();
    }
  }

  private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  configurePathMatchers(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg) {

    for (PathMatcherProperties props : authProperties.preparePathMatchers(corsProperties)) {
      log.info("Securing requests to {}", props);
      HttpMethod httpMethod = props.httpMethod();
      if (httpMethod == null) {
        reg = reg.antMatchers(props.getAntPattern())
            .access(props.accessExpression(
                authProperties::ensureRolePrefix));
      } else {
        reg = reg.antMatchers(httpMethod, props.getAntPattern())
            .access(props.accessExpression(
                authProperties::ensureRolePrefix));
      }
    }
    return reg;
  }

  private HttpSecurity configureAuthenticationProvider(HttpSecurity http) throws Exception {

    if (jwtConverterProvider.getIfAvailable() != null) {
      log.info("Configure authentication provider with JWT.");
      return http
          .oauth2ResourceServer((rs) -> rs
              .jwt()
              .jwtAuthenticationConverter(jwtConverterProvider.getIfAvailable())
              .and());
    }
    log.info("Configure authentication provider with basic auth and user details service.");
    String realm = environment.getProperty("spring.application.name", "Restricted area");
    return http
        .formLogin().disable()
        .httpBasic().realmName(realm)
        .and();
  }

}
