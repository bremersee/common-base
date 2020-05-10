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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.web.CorsProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The resource server security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnExpression(
    "'${bremersee.auth.resource-server:OTHER}' != T(org.bremersee.security.authentication.AutoSecurityMode).OTHER")
@ConditionalOnClass({
    HttpSecurity.class,
    PasswordFlowProperties.class
})
@ConditionalOnMissingBean(type = {
    "org.bremersee.actuator.security.authentication.ResourceServerWithActuatorAutoConfiguration"
})
@EnableConfigurationProperties({
    CorsProperties.class,
    SecurityProperties.class,
    AuthProperties.class})
@Configuration
@Slf4j
public class ResourceServerAutoConfiguration extends AbstractResourceServerAutoConfiguration {

  /**
   * Instantiates a new resource server security auto configuration.
   *
   * @param environment the environment
   * @param securityProperties the spring properties
   * @param authProperties the security properties
   * @param corsProperties the cors properties
   * @param jwtConverterProvider the jwt converter provider
   * @param passwordEncoderProvider the password encoder provider
   */
  public ResourceServerAutoConfiguration(
      Environment environment,
      SecurityProperties securityProperties,
      AuthProperties authProperties,
      CorsProperties corsProperties,
      ObjectProvider<JsonPathJwtConverter> jwtConverterProvider,
      ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
    super(environment, securityProperties, authProperties, corsProperties, jwtConverterProvider,
        passwordEncoderProvider);
  }

  @EventListener(ApplicationReadyEvent.class)
  @Override
  public void init() {
    super.init();
  }

  @Override
  protected ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry init(
      HttpSecurity httpSecurity) throws Exception {
    return httpSecurity.authorizeRequests();
  }

  @ConditionalOnExpression("'${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}'.empty")
  @ConditionalOnMissingBean
  @Bean
  @Override
  public UserDetailsService userDetailsServiceBean() {
    return new InMemoryUserDetailsAutoConfiguration().inMemoryUserDetailsManager(
        getSecurityProperties(),
        getAuthProperties(),
        getPasswordEncoderProvider());
  }

}
