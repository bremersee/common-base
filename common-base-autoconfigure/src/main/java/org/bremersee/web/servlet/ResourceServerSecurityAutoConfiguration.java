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

package org.bremersee.web.servlet;

import static org.bremersee.security.SecurityProperties.AuthenticationProperties.ApplicationAccessProperties.ALL_HTTP_METHODS;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.ApplicationAccessProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.ApplicationAccessProperties.PathMatcherProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The resource server security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(
    prefix = "bremersee.security",
    name = "resource-server-auto-configuration",
    havingValue = "true")
@ConditionalOnClass({
    HttpSecurity.class
})
@EnableConfigurationProperties(SecurityProperties.class)
@Configuration
@Order(51)
@Slf4j
public class ResourceServerSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

  private Environment environment;

  private SecurityProperties securityProperties;

  private ObjectProvider<JsonPathJwtConverter> jwtConverterProvider;

  /**
   * Instantiates a new resource server security auto configuration.
   *
   * @param environment the environment
   * @param securityProperties the security properties
   * @param jwtConverterProvider the jwt converter provider
   */
  public ResourceServerSecurityAutoConfiguration(
      Environment environment,
      SecurityProperties securityProperties,
      ObjectProvider<JsonPathJwtConverter> jwtConverterProvider) {

    this.environment = environment;
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

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    HttpSecurity http = httpSecurity;
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg = http
        .requestMatcher(new NegatedRequestMatcher(EndpointRequest.toAnyEndpoint()))
        .authorizeRequests();
    reg = configurePermitAll(reg);
    reg = configureAdminAccess(reg);
    reg = configureUserAccess(reg);
    http = configureAuthenticationProvider(reg.and());
    http = http.csrf().disable();
    if (securityProperties.getCors().isDisabled()) {
      http.cors().disable();
    }
  }

  private HttpSecurity configureAuthenticationProvider(HttpSecurity http) throws Exception {

    if (securityProperties.getAuthentication().isEnableJwtSupport()) {
      log.info("Configure authentication provider with JWT.");
      JsonPathJwtConverter jwtConverter = jwtConverterProvider.getIfAvailable();
      Assert.notNull(jwtConverter, "JWT converter must be present.");
      return http
          .oauth2ResourceServer((rs) -> rs
              .jwt()
              .jwtAuthenticationConverter(jwtConverter)
              .and());
    }
    log.info("Configure authentication provider with basic auth and in-memory users.");
    String realm = environment.getProperty("spring.application.name", "Restricted area");
    return http
        .authenticationProvider(inMemoryAuthenticationProvider())
        .httpBasic()
        .realmName(realm)
        .and()
        .formLogin().disable()
        .sessionManagement((sm) -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
  }

  private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  configureUserAccess(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg) {

    log.info("Configure user access ...");
    ApplicationAccessProperties props = securityProperties.getAuthentication().getApplication();
    RequestMatcher[] matchers = matchers(props.getUserMatchers());
    Set<String> roles = props.userRolesOrDefaults(
        securityProperties.getAuthentication()::ensureRolePrefix);
    String accessExpr = props.buildAccessExpression(
        roles,
        null,
        false,
        null);
    log.info("Configure user access with expression {}.", accessExpr);
    if (matchers.length > 0) {
      return reg.requestMatchers(matchers).access(accessExpr)
          .anyRequest().denyAll();
    }
    return reg.anyRequest().access(accessExpr);
  }

  private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  configureAdminAccess(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg) {

    log.info("Configure admin access ...");
    ApplicationAccessProperties props = securityProperties.getAuthentication().getApplication();
    RequestMatcher[] matchers = matchers(props.getAdminMatchers());
    if (matchers.length > 0) {
      Set<String> roles = props.adminRolesOrDefaults(
          securityProperties.getAuthentication()::ensureRolePrefix,
          AuthorityConstants.ADMIN_ROLE_NAME);
      String accessExpr = props.buildAccessExpression(
          roles,
          null,
          false,
          null);
      log.info("Configure admin access with expression {}.", accessExpr);
      return reg.requestMatchers(matchers).access(accessExpr);
    } else {
      log.info("Configure admin access: Nothing to do because there are no path matchers.");
      return reg;
    }
  }

  private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  configurePermitAll(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg) {

    log.info("Configure permit all ...");
    ApplicationAccessProperties props = securityProperties.getAuthentication().getApplication();
    return Optional.of(matchers(props.getPermitAllMatchers()))
        .filter(matchers -> matchers.length > 0)
        .map(matchers -> reg.requestMatchers(matchers).permitAll())
        .orElse(reg);
  }

  private RequestMatcher[] matchers(
      Collection<? extends PathMatcherProperties> properties) {

    return Optional.ofNullable(properties)
        .map(col -> col.stream()
            .sorted()
            .peek(props -> log.info("Using request matcher {}.", props))
            .map(props -> ALL_HTTP_METHODS.equals(props.getHttpMethod())
                ? new AntPathRequestMatcher(props.getAntPattern())
                : new AntPathRequestMatcher(props.getAntPattern(), props.getHttpMethod()))
            .toArray(RequestMatcher[]::new))
        .orElseGet(() -> new RequestMatcher[0]);
  }

  private AuthenticationProvider inMemoryAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(securityProperties.getAuthentication().passwordEncoder());
    provider.setUserDetailsPasswordService(new InMemoryUserDetailsManager(
        securityProperties.getAuthentication().buildBasicAuthUserDetails()));
    return provider;
  }

}
