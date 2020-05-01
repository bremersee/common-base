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

package org.bremersee.actuator.web.servlet;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.actuator.security.ActuatorSecurityProperties;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.authentication.PasswordFlowAuthenticationManager;
import org.bremersee.security.authentication.RestTemplateAccessTokenRetriever;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
@ConditionalOnProperty(
    prefix = "bremersee.actuator.security",
    name = "enable-auto-configuration",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnClass({
    HttpSecurity.class,
    Info.class
})
@EnableConfigurationProperties({
    SecurityProperties.class,
    ActuatorSecurityProperties.class})
@Configuration
@Order(61)
@Slf4j
public class ActuatorSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

  private SecurityProperties securityProperties;

  private ActuatorSecurityProperties actuatorSecurityProperties;

  private ObjectProvider<JwtDecoder> jwtDecoderProvider;

  private ObjectProvider<JsonPathJwtConverter> jsonPathJwtConverterProvider;

  private ObjectProvider<RestTemplateAccessTokenRetriever> tokenRetrieverProvider;

  /**
   * Instantiates a new actuator security auto configuration.
   *
   * @param securityProperties the security properties
   * @param actuatorSecurityProperties the actuator security properties
   * @param jwtDecoderProvider the jwt decoder provider
   * @param jsonPathJwtConverterProvider the json path jwt converter provider
   * @param tokenRetrieverProvider the token retriever provider
   */
  public ActuatorSecurityAutoConfiguration(
      SecurityProperties securityProperties,
      ActuatorSecurityProperties actuatorSecurityProperties,
      ObjectProvider<JwtDecoder> jwtDecoderProvider,
      ObjectProvider<JsonPathJwtConverter> jsonPathJwtConverterProvider,
      ObjectProvider<RestTemplateAccessTokenRetriever> tokenRetrieverProvider) {

    this.securityProperties = securityProperties;
    this.actuatorSecurityProperties = actuatorSecurityProperties;
    this.jwtDecoderProvider = jwtDecoderProvider;
    this.jsonPathJwtConverterProvider = jsonPathJwtConverterProvider;
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

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    log.info("Securing requests to /actuator/**");
    httpSecurity
        .requestMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeRequests()
        .requestMatchers(unauthenticatedEndpointMatchers()).permitAll()
        .requestMatchers(new AndRequestMatcher(
            EndpointRequest.toAnyEndpoint(),
            new AntPathRequestMatcher("/**", "GET")))
        .access(actuatorSecurityProperties.buildAccessExpression())
        .anyRequest()
        .access(actuatorSecurityProperties.buildAdminAccessExpression())
        .and()
        .csrf().disable()
        .cors(customizer -> {
          if (!actuatorSecurityProperties.isEnableCors()) {
            customizer.disable();
          }
        })
        .authenticationProvider(authenticationProvider())
        .httpBasic()
        .realmName("actuator")
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  private EndpointRequest.EndpointRequestMatcher[] unauthenticatedEndpointMatchers() {
    return actuatorSecurityProperties.unauthenticatedEndpointsOrDefaults().stream()
        .map(EndpointRequest::to)
        .toArray(EndpointRequest.EndpointRequestMatcher[]::new);
  }

  private AuthenticationProvider authenticationProvider() {
    if (actuatorSecurityProperties.isEnableJwtSupport()) {
      return passwordFlowAuthenticationProvider();
    }
    return inMemoryAuthenticationProvider();
  }

  @SuppressWarnings("DuplicatedCode")
  private PasswordFlowAuthenticationManager passwordFlowAuthenticationProvider() {

    JwtDecoder jwtDecoder;
    if (StringUtils.hasText(actuatorSecurityProperties.getJwkUriSet())) {
      NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
          .withJwkSetUri(actuatorSecurityProperties.getJwkUriSet())
          .jwsAlgorithm(SignatureAlgorithm.from(actuatorSecurityProperties.getJwsAlgorithm()))
          .build();
      if (StringUtils.hasText(actuatorSecurityProperties.getIssuerUri())) {
        nimbusJwtDecoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer(actuatorSecurityProperties.getIssuerUri()));
      }
      jwtDecoder = nimbusJwtDecoder;
    } else {
      jwtDecoder = jwtDecoderProvider.getIfAvailable();
      Assert.notNull(jwtDecoder, "JWT decoder must be present.");
    }

    JsonPathJwtConverter internalJwtConverter = new JsonPathJwtConverter();
    internalJwtConverter.setNameJsonPath(actuatorSecurityProperties.getNameJsonPath());
    internalJwtConverter.setRolePrefix(actuatorSecurityProperties.getRolePrefix());
    internalJwtConverter.setRolesJsonPath(actuatorSecurityProperties.getRolesJsonPath());
    internalJwtConverter.setRolesValueList(actuatorSecurityProperties.isRolesValueList());
    internalJwtConverter.setRolesValueSeparator(
        actuatorSecurityProperties.getRolesValueSeparator());
    JsonPathJwtConverter externalJwtConverter = jsonPathJwtConverterProvider.getIfAvailable();
    JsonPathJwtConverter jwtConverter;
    if (internalJwtConverter.equals(externalJwtConverter)) {
      jwtConverter = externalJwtConverter;
    } else {
      jwtConverter = internalJwtConverter;
    }

    return new PasswordFlowAuthenticationManager(
        actuatorSecurityProperties.getPasswordFlow(),
        jwtDecoder,
        jwtConverter,
        tokenRetrieverProvider
            .getIfAvailable(() -> new RestTemplateAccessTokenRetriever(new RestTemplate())));
  }

  private AuthenticationProvider inMemoryAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(securityProperties.getAuthentication().passwordEncoder());
    provider.setUserDetailsPasswordService(new InMemoryUserDetailsManager(
        securityProperties.getAuthentication().buildBasicAuthUserDetails()));
    return provider;
  }

}
