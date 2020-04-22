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

package org.bremersee.base.webmvc.app;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.authentication.AuthenticationProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.authentication.PasswordFlowAuthenticationManager;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackageClasses = {TestConfiguration.class})
public class TestConfiguration {

  /**
   * The resource server security configuration.
   */
  @Order(51)
  @Configuration
  @Slf4j
  static class ResourceServer extends WebSecurityConfigurerAdapter {

    private JsonPathJwtConverter jwtConverter;

    /**
     * Instantiates a new resource server security configuration.
     *
     * @param jwtConverter the jwt converter
     */
    @Autowired
    public ResourceServer(
        ObjectProvider<JsonPathJwtConverter> jwtConverter) {
      this.jwtConverter = jwtConverter.getIfAvailable();
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
      log.info("msg=[Using jwt authentication.]");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
      log.info("Authorizing requests to /api/** with OAuth2.");
      http
          .requestMatcher(new NegatedRequestMatcher(EndpointRequest.toAnyEndpoint()))
          .csrf().disable()
          .authorizeRequests()
          .antMatchers(HttpMethod.OPTIONS).permitAll()
          .antMatchers("/api/exception").permitAll()
          .antMatchers("/api/admin/**").hasAuthority(AuthorityConstants.ADMIN_ROLE_NAME)
          .anyRequest()
          .authenticated();
      http
          .oauth2ResourceServer()
          .jwt()
          .jwtAuthenticationConverter(jwtConverter);
    }
  }

  /**
   * The actuator security configuration.
   */
  @Order(52)
  @Configuration
  @Slf4j
  @EnableConfigurationProperties(AuthenticationProperties.class)
  static class Actuator extends WebSecurityConfigurerAdapter {

    private final AuthenticationProperties properties;

    private final PasswordFlowAuthenticationManager passwordFlowAuthenticationManager;

    /**
     * Instantiates a new actuator security configuration.
     *
     * @param properties the properties
     * @param passwordFlowAuthenticationManager the password flow authentication manager
     */
    @Autowired
    public Actuator(
        final AuthenticationProperties properties,
        final ObjectProvider<PasswordFlowAuthenticationManager> passwordFlowAuthenticationManager) {
      this.properties = properties;
      this.passwordFlowAuthenticationManager = passwordFlowAuthenticationManager.getIfAvailable();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      log.info("Authorizing requests to /actuator/** with password flow auth.");
      http
          .requestMatcher(EndpointRequest.toAnyEndpoint())
          .csrf().disable()
          .authenticationProvider(passwordFlowAuthenticationManager)
          .httpBasic()
          .realmName("actuator")
          .and()
          .requestMatcher(EndpointRequest.toAnyEndpoint())
          .authorizeRequests()
          .antMatchers(HttpMethod.OPTIONS).permitAll()
          .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
          // .requestMatchers(EndpointRequest.to(InfoEndpoint.class)).permitAll()
          .anyRequest()
          .access(properties.getActuator().buildAccessExpression(null))
          .and()
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
  }

}
