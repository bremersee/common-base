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

package org.bremersee.actuator.security.authentication;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.security.authentication.AuthProperties;
import org.bremersee.security.authentication.AutoSecurityMode;
import org.bremersee.security.authentication.InMemoryUserDetailsAutoConfiguration;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.authentication.PasswordFlowAuthenticationManager;
import org.bremersee.security.authentication.PasswordFlowProperties;
import org.bremersee.security.authentication.RestTemplateAccessTokenRetriever;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * The actuator security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@Conditional({ActuatorAutoSecurityCondition.class})
@ConditionalOnClass({
    HttpSecurity.class,
    PasswordFlowProperties.class,
    Info.class
})
@EnableConfigurationProperties({
    SecurityProperties.class,
    AuthProperties.class,
    ActuatorAuthProperties.class})
@Configuration
@Slf4j
public class ActuatorSecurityAutoConfiguration extends WebSecurityConfigurerAdapter
    implements Ordered {

  private final SecurityProperties securityProperties;

  private final AuthProperties authProperties;

  private final ActuatorAuthProperties actuatorAuthProperties;

  private final ObjectProvider<JsonPathJwtConverter> jsonPathJwtConverterProvider;

  private final ObjectProvider<RestTemplateAccessTokenRetriever> tokenRetrieverProvider;

  private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

  /**
   * Instantiates a new actuator security auto configuration.
   *
   * @param securityProperties the security properties
   * @param authProperties the security properties
   * @param actuatorAuthProperties the actuator security properties
   * @param jsonPathJwtConverterProvider the json path jwt converter provider
   * @param tokenRetrieverProvider the token retriever provider
   * @param passwordEncoderProvider the password encoder provider
   */
  public ActuatorSecurityAutoConfiguration(
      SecurityProperties securityProperties,
      AuthProperties authProperties,
      ActuatorAuthProperties actuatorAuthProperties,
      ObjectProvider<JsonPathJwtConverter> jsonPathJwtConverterProvider,
      ObjectProvider<RestTemplateAccessTokenRetriever> tokenRetrieverProvider,
      ObjectProvider<PasswordEncoder> passwordEncoderProvider) {

    this.securityProperties = securityProperties;
    this.authProperties = authProperties;
    this.actuatorAuthProperties = actuatorAuthProperties;
    this.jsonPathJwtConverterProvider = jsonPathJwtConverterProvider;
    this.tokenRetrieverProvider = tokenRetrieverProvider;
    this.passwordEncoderProvider = passwordEncoderProvider;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  @SuppressWarnings("DuplicatedCode")
  public void init() {
    final boolean hasJwkUriSet = StringUtils.hasText(actuatorAuthProperties.getJwkSetUri());
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
        actuatorAuthProperties.getEnable().name(),
        actuatorAuthProperties.getOrder(),
        hasJwkUriSet,
        actuatorAuthProperties.isEnableCors());
    if (hasJwkUriSet) {
      Assert.hasText(actuatorAuthProperties.getPasswordFlow().getTokenEndpoint(),
          "Token endpoint of actuator password flow must be present.");
      Assert.hasText(actuatorAuthProperties.getPasswordFlow().getClientId(),
          "Client ID of actuator password flow must be present.");
      Assert.notNull(actuatorAuthProperties.getPasswordFlow().getClientSecret(),
          "Client secret of actuator password flow must be present.");
    }
  }

  @Override
  public int getOrder() {
    return actuatorAuthProperties.getOrder();
  }

  private EndpointRequest.EndpointRequestMatcher[] unauthenticatedEndpointMatchers() {
    return actuatorAuthProperties.unauthenticatedEndpointsOrDefaults().stream()
        .map(EndpointRequest::to)
        .toArray(EndpointRequest.EndpointRequestMatcher[]::new);
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    log.info("Securing requests to /actuator/**");
    HttpSecurity http = httpSecurity;
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg = http
        .requestMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeRequests();
    if (actuatorAuthProperties.getEnable() == AutoSecurityMode.NONE) {
      http = reg
          .anyRequest().permitAll()
          .and()
          .httpBasic().disable();
    } else {
      if (actuatorAuthProperties.isEnableCors()) {
        reg = reg.antMatchers(HttpMethod.OPTIONS, "/**").permitAll();
      }
      http = reg
          .requestMatchers(unauthenticatedEndpointMatchers()).permitAll()
          .requestMatchers(new AndRequestMatcher(
              EndpointRequest.toAnyEndpoint(),
              new AntPathRequestMatcher("/**", "GET")))
          .access(actuatorAuthProperties.buildAccessExpression())
          .anyRequest()
          .access(actuatorAuthProperties.buildAdminAccessExpression())
          .and();
      if (StringUtils.hasText(actuatorAuthProperties.getJwkSetUri())) {
        http.authenticationProvider(passwordFlowAuthenticationManager());
      }
      http = http
          .formLogin().disable()
          .httpBasic().realmName("actuator")
          .and();
    }
    http
        .csrf().disable()
        .cors(customizer -> {
          if (!actuatorAuthProperties.isEnableCors()) {
            customizer.disable();
          }
        })
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  @ConditionalOnExpression("'${bremersee.actuator.auth.jwk-set-uri:}'.empty")
  @ConditionalOnMissingBean
  @Bean
  @Override
  public UserDetailsService userDetailsServiceBean() {
    return new InMemoryUserDetailsAutoConfiguration().inMemoryUserDetailsManager(
        securityProperties,
        authProperties,
        passwordEncoderProvider);
  }

  private PasswordFlowAuthenticationManager passwordFlowAuthenticationManager() {
    RestTemplateAccessTokenRetriever tokenRetriever = tokenRetrieverProvider.getIfAvailable();
    log.info("Creating actuator {} with token retriever {} ...",
        PasswordFlowAuthenticationManager.class.getSimpleName(), tokenRetriever);
    return new PasswordFlowAuthenticationManager(
        actuatorAuthProperties.getPasswordFlow(),
        jwtDecoder(),
        jwtConverter(),
        Objects.requireNonNullElseGet(
            tokenRetriever,
            () -> new RestTemplateAccessTokenRetriever(new RestTemplate())));
  }

  private JwtDecoder jwtDecoder() {
    NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
        .withJwkSetUri(actuatorAuthProperties.getJwkSetUri())
        .jwsAlgorithm(SignatureAlgorithm.from(actuatorAuthProperties.getJwsAlgorithm()))
        .build();
    if (StringUtils.hasText(actuatorAuthProperties.getIssuerUri())) {
      nimbusJwtDecoder.setJwtValidator(
          JwtValidators.createDefaultWithIssuer(actuatorAuthProperties.getIssuerUri()));
    }
    return nimbusJwtDecoder;
  }

  private JsonPathJwtConverter jwtConverter() {
    JsonPathJwtConverter internalJwtConverter = new JsonPathJwtConverter();
    internalJwtConverter.setNameJsonPath(actuatorAuthProperties.getNameJsonPath());
    internalJwtConverter.setRolePrefix(actuatorAuthProperties.getRolePrefix());
    internalJwtConverter.setRolesJsonPath(actuatorAuthProperties.getRolesJsonPath());
    internalJwtConverter.setRolesValueList(actuatorAuthProperties.isRolesValueList());
    internalJwtConverter.setRolesValueSeparator(
        actuatorAuthProperties.getRolesValueSeparator());
    JsonPathJwtConverter externalJwtConverter = jsonPathJwtConverterProvider.getIfAvailable();
    JsonPathJwtConverter jwtConverter;
    if (internalJwtConverter.equals(externalJwtConverter)) {
      jwtConverter = externalJwtConverter;
    } else {
      jwtConverter = internalJwtConverter;
    }
    return jwtConverter;
  }

}
