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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.PathMatcherProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The resource server security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(
    prefix = "bremersee.security.authentication",
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
    reg = configurePathMatchers(reg);
    http = configureAuthenticationProvider(reg.and());
    http = http.csrf().disable();
    if (!securityProperties.getCors().isEnable()) {
      http.cors().disable();
    }
  }

  private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  configurePathMatchers(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg) {

    for (PathMatcherProperties props : securityProperties.getAuthentication().pathMatchers()) {
      log.info("Securing requests to {}", props);
      HttpMethod httpMethod = props.httpMethod();
      if (httpMethod == null) {
        reg = reg.antMatchers(props.getAntPattern())
            .access(props.accessExpression(
                securityProperties.getAuthentication()::ensureRolePrefix));
      } else {
        reg = reg.antMatchers(httpMethod, props.getAntPattern())
            .access(props.accessExpression(
                securityProperties.getAuthentication()::ensureRolePrefix));
      }
    }
    return reg;
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
        .userDetailsService(userDetailsService())
        .formLogin().disable()
        .httpBasic().realmName(realm)
        .and()
        .sessionManagement((sm) -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
  }

  @Override
  protected UserDetailsService userDetailsService() {
    if (securityProperties.getAuthentication().isEnableJwtSupport()) {
      return super.userDetailsService();
    }
    return new InMemoryUserDetailsManager(
        securityProperties.getAuthentication().buildBasicAuthUserDetails());
  }

}
