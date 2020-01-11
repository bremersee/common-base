/*
 * Copyright 2019 the original author or authors.
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;

/**
 * The authentication support auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(
    prefix = "bremersee.security.authentication",
    name = "enable-jwt-support",
    havingValue = "true")
@ConditionalOnClass({
    org.springframework.boot.web.client.RestTemplateBuilder.class,
    org.bremersee.security.authentication.JsonPathJwtConverter.class,
    org.bremersee.security.authentication.RestTemplateAccessTokenRetriever.class
})
@EnableConfigurationProperties(AuthenticationProperties.class)
@Configuration
@Slf4j
public class AuthenticationSupportAutoConfiguration {

  private AuthenticationProperties properties;

  /**
   * Instantiates a new authentication support auto configuration.
   *
   * @param properties the properties
   */
  public AuthenticationSupportAutoConfiguration(
      AuthenticationProperties properties) {
    this.properties = properties;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* rolesJsonPath = {}\n"
            + "* rolesValueList = {}\n"
            + "* rolesValueSeparator = {}\n"
            + "* rolePrefix = {}\n"
            + "* nameJsonPath = {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName(),
        properties.getRolesJsonPath(),
        properties.isRolesValueList(),
        properties.getRolesValueSeparator(),
        properties.getRolePrefix(),
        properties.getNameJsonPath());
  }

  /**
   * Json path jwt converter json path jwt converter.
   *
   * @return the json path jwt converter
   */
  @ConditionalOnMissingBean
  @Bean
  public JsonPathJwtConverter jsonPathJwtConverter() {
    log.info("Creating {} ...", JsonPathJwtConverter.class.getSimpleName());
    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setNameJsonPath(properties.getNameJsonPath());
    converter.setRolePrefix(properties.getRolePrefix());
    converter.setRolesJsonPath(properties.getRolesJsonPath());
    converter.setRolesValueList(properties.isRolesValueList());
    converter.setRolesValueSeparator(properties.getRolesValueSeparator());
    return converter;
  }

  /**
   * Rest template access token retriever rest template access token retriever.
   *
   * @param restTemplateBuilder the rest template builder
   * @return the rest template access token retriever
   */
  @ConditionalOnMissingBean
  @Bean
  public RestTemplateAccessTokenRetriever restTemplateAccessTokenRetriever(
      ObjectProvider<RestTemplateBuilder> restTemplateBuilder) {

    Assert.notNull(
        restTemplateBuilder.getIfAvailable(),
        "Rest template builder must be present.");
    log.info("Creating {} ...", RestTemplateAccessTokenRetriever.class.getSimpleName());
    return new RestTemplateAccessTokenRetriever(restTemplateBuilder.getIfAvailable().build());
  }

  /**
   * Password flow authentication manager password flow authentication manager.
   *
   * @param jwtDecoder the jwt decoder
   * @param jwtConverter the jwt converter
   * @param tokenRetriever the token retriever
   * @return the password flow authentication manager
   */
  @ConditionalOnMissingBean
  @Bean
  public PasswordFlowAuthenticationManager passwordFlowAuthenticationManager(
      ObjectProvider<JwtDecoder> jwtDecoder,
      JsonPathJwtConverter jwtConverter,
      RestTemplateAccessTokenRetriever tokenRetriever) {

    Assert.notNull(
        jwtDecoder.getIfAvailable(),
        "Jwt decoder must be present.");

    log.info("Creating {} ...", PasswordFlowAuthenticationManager.class.getSimpleName());
    return new PasswordFlowAuthenticationManager(
        properties,
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
