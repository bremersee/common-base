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

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.core.OrderedProxy;
import org.bremersee.security.authentication.AuthProperties;
import org.bremersee.security.authentication.AutoSecurityMode;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.PasswordFlowProperties;
import org.bremersee.security.authentication.PasswordFlowReactiveAuthenticationManager;
import org.bremersee.security.authentication.RoleBasedAuthorizationManager;
import org.bremersee.security.authentication.RoleOrIpBasedAuthorizationManager;
import org.bremersee.security.authentication.WebClientAccessTokenRetriever;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest.EndpointServerWebExchangeMatcher;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * The reactive actuator security auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnExpression(
    "'${bremersee.actuator.auth.enable:OTHER}' != T(org.bremersee.security.authentication.AutoSecurityMode).OTHER")
@ConditionalOnClass({
    ServerHttpSecurity.class,
    ReactiveAuthenticationManager.class,
    PasswordFlowProperties.class,
    Info.class
})
@EnableConfigurationProperties({
    SecurityProperties.class,
    AuthProperties.class,
    ActuatorAuthProperties.class})
@Configuration
@Slf4j
public class ReactiveActuatorSecurityAutoConfiguration {

  private final AuthProperties authProperties;

  private final ActuatorAuthProperties actuatorAuthProperties;

  private final ObjectProvider<JsonPathReactiveJwtConverter> jsonPathJwtConverterProvider;

  private final ObjectProvider<WebClientAccessTokenRetriever> tokenRetrieverProvider;

  private final ObjectProvider<ReactiveUserDetailsService> userDetailsServiceProvider;

  private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

  /**
   * Instantiates a new reactive actuator security auto configuration.
   *
   * @param authProperties the security properties
   * @param actuatorAuthProperties the actuator security properties
   * @param jsonPathJwtConverterProvider the json path jwt converter provider
   * @param tokenRetrieverProvider the token retriever provider
   * @param userDetailsServiceProvider the user details service provider
   * @param passwordEncoderProvider the password encoder provider
   */
  public ReactiveActuatorSecurityAutoConfiguration(
      AuthProperties authProperties,
      ActuatorAuthProperties actuatorAuthProperties,
      ObjectProvider<JsonPathReactiveJwtConverter> jsonPathJwtConverterProvider,
      ObjectProvider<WebClientAccessTokenRetriever> tokenRetrieverProvider,
      ObjectProvider<ReactiveUserDetailsService> userDetailsServiceProvider,
      ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
    this.authProperties = authProperties;
    this.actuatorAuthProperties = actuatorAuthProperties;
    this.jsonPathJwtConverterProvider = jsonPathJwtConverterProvider;
    this.tokenRetrieverProvider = tokenRetrieverProvider;
    this.userDetailsServiceProvider = userDetailsServiceProvider;
    this.passwordEncoderProvider = passwordEncoderProvider;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  @SuppressWarnings("DuplicatedCode")
  public void init() {
    final boolean hasJwkUriSet = StringUtils.hasText(actuatorAuthProperties.getJwkUriSet());
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

  /**
   * Actuator filter chain security web filter chain.
   *
   * @param httpProvider the http provider
   * @return the security web filter chain
   */
  @Bean
  public SecurityWebFilterChain actuatorFilterChain(
      ObjectProvider<ServerHttpSecurity> httpProvider) {

    ServerHttpSecurity http = httpProvider.getIfAvailable();
    Assert.notNull(http, "Server http security must be present.");
    log.info("Securing requests to /actuator/**");
    AuthorizeExchangeSpec spec = http
        .securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeExchange();
    if (actuatorAuthProperties.getEnable() == AutoSecurityMode.NONE) {
      http = spec
          .anyExchange().permitAll()
          .and()
          .httpBasic().disable();
    } else {
      http = spec
          .matchers(unauthenticatedEndpointMatchers()).permitAll()
          .matchers(new AndServerWebExchangeMatcher(
              EndpointRequest.toAnyEndpoint(),
              ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**")))
          .access(new RoleOrIpBasedAuthorizationManager(
              actuatorAuthProperties.rolesOrDefaults(),
              actuatorAuthProperties.getIpAddresses()))
          .anyExchange()
          .access(new RoleBasedAuthorizationManager(
              actuatorAuthProperties.adminRolesOrDefaults()))
          .and()
          .authenticationManager(authenticationManager())
          .httpBasic()
          .and()
          .formLogin().disable();
    }
    http = http
        .csrf().disable()
        .cors(customizer -> {
          if (!actuatorAuthProperties.isEnableCors()) {
            customizer.disable();
          }
        });
    return OrderedProxy.create(http.build(), actuatorAuthProperties.getOrder());
  }

  private EndpointServerWebExchangeMatcher[] unauthenticatedEndpointMatchers() {
    return actuatorAuthProperties.unauthenticatedEndpointsOrDefaults().stream()
        .map(EndpointRequest::to)
        .toArray(EndpointServerWebExchangeMatcher[]::new);
  }

  private ReactiveAuthenticationManager authenticationManager() {
    return StringUtils.hasText(actuatorAuthProperties.getJwkUriSet())
        ? passwordFlowReactiveAuthenticationManager()
        : userDetailsAuthenticationManager();
  }

  private ReactiveAuthenticationManager userDetailsAuthenticationManager() {
    return Optional.ofNullable(userDetailsServiceProvider.getIfAvailable())
        .map(UserDetailsRepositoryReactiveAuthenticationManager::new)
        .orElseGet(() -> new UserDetailsRepositoryReactiveAuthenticationManager(
            new MapReactiveUserDetailsService(authProperties
                .buildBasicAuthUserDetails(passwordEncoderProvider.getIfAvailable()))));
  }

  private PasswordFlowReactiveAuthenticationManager passwordFlowReactiveAuthenticationManager() {
    return new PasswordFlowReactiveAuthenticationManager(
        actuatorAuthProperties.getPasswordFlow(),
        jwtDecoder(),
        jwtConverter(),
        tokenRetrieverProvider.getIfAvailable(WebClientAccessTokenRetriever::new));
  }

  private ReactiveJwtDecoder jwtDecoder() {
    NimbusReactiveJwtDecoder nimbusJwtDecoder = NimbusReactiveJwtDecoder
        .withJwkSetUri(actuatorAuthProperties.getJwkUriSet())
        .jwsAlgorithm(SignatureAlgorithm.from(actuatorAuthProperties.getJwsAlgorithm()))
        .build();
    if (StringUtils.hasText(actuatorAuthProperties.getIssuerUri())) {
      nimbusJwtDecoder.setJwtValidator(
          JwtValidators.createDefaultWithIssuer(actuatorAuthProperties.getIssuerUri()));
    }
    return nimbusJwtDecoder;
  }

  private JsonPathReactiveJwtConverter jwtConverter() {
    JsonPathJwtConverter tmpJwtConverter = new JsonPathJwtConverter();
    tmpJwtConverter.setNameJsonPath(actuatorAuthProperties.getNameJsonPath());
    tmpJwtConverter.setRolePrefix(actuatorAuthProperties.getRolePrefix());
    tmpJwtConverter.setRolesJsonPath(actuatorAuthProperties.getRolesJsonPath());
    tmpJwtConverter.setRolesValueList(actuatorAuthProperties.isRolesValueList());
    tmpJwtConverter.setRolesValueSeparator(actuatorAuthProperties.getRolesValueSeparator());
    JsonPathReactiveJwtConverter internalJwtConverter = new JsonPathReactiveJwtConverter(
        tmpJwtConverter);
    JsonPathReactiveJwtConverter externalJwtConverter = jsonPathJwtConverterProvider
        .getIfAvailable();
    JsonPathReactiveJwtConverter jwtConverter;
    if (internalJwtConverter.equals(externalJwtConverter)) {
      log.info("Actuator security is using jwt converter from main application.");
      jwtConverter = externalJwtConverter;
    } else {
      log.info("Actuator security is using it's own jwt converter.");
      jwtConverter = internalJwtConverter;
    }
    return jwtConverter;
  }

}

