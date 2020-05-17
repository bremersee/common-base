/*
 * Copyright 2019-2020 the original author or authors.
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The authentication support auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({
    RestTemplateBuilder.class,
    JsonPathJwtConverter.class,
    RestTemplateAccessTokenRetriever.class
})
@EnableConfigurationProperties(AuthProperties.class)
@Configuration
@Slf4j
public class JwtSupportAutoConfiguration {

  private final AuthProperties properties;

  /**
   * Instantiates a new authentication support auto configuration.
   *
   * @param properties the properties
   */
  public JwtSupportAutoConfiguration(
      AuthProperties properties) {
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
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        properties.getRolesJsonPath(),
        properties.isRolesValueList(),
        properties.getRolesValueSeparator(),
        properties.getRolePrefix(),
        properties.getNameJsonPath());
  }

  /**
   * Creates a json path jwt converter bean.
   *
   * @return the json path jwt converter
   */
  @ConditionalOnProperty(
      prefix = "spring.security.oauth2.resourceserver.jwt",
      name = "jwk-set-uri")
  @ConditionalOnMissingBean
  @Bean
  @SuppressWarnings("DuplicatedCode")
  public JsonPathJwtConverter jsonPathJwtConverter() {
    log.info("Creating application {} ...", JsonPathJwtConverter.class.getSimpleName());
    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setNameJsonPath(properties.getNameJsonPath());
    converter.setRolePrefix(properties.getRolePrefix());
    converter.setRolesJsonPath(properties.getRolesJsonPath());
    converter.setRolesValueList(properties.isRolesValueList());
    converter.setRolesValueSeparator(properties.getRolesValueSeparator());
    return converter;
  }

  /**
   * Creates access token retriever.
   *
   * @param restTemplateBuilder the rest template builder
   * @param accessTokenCache the access token cache
   * @return the rest template access token retriever
   */
  @Conditional(JwtSupportCondition.class)
  @ConditionalOnMissingBean
  @Bean
  public RestTemplateAccessTokenRetriever restTemplateAccessTokenRetriever(
      ObjectProvider<RestTemplateBuilder> restTemplateBuilder,
      ObjectProvider<AccessTokenCache> accessTokenCache) {

    AccessTokenCache cache = accessTokenCache.getIfAvailable();
    log.info("Creating common {} with cache {} ...",
        RestTemplateAccessTokenRetriever.class.getSimpleName(), cache);
    Assert.notNull(
        restTemplateBuilder.getIfAvailable(),
        "Rest template builder must be present.");
    return new RestTemplateAccessTokenRetriever(
        restTemplateBuilder.getIfAvailable().build(),
        cache);
  }

  /**
   * Creates a password flow authentication manager.
   *
   * @param jwtDecoder the jwt decoder
   * @param jwtConverter the jwt converter
   * @param tokenRetriever the token retriever
   * @return the password flow authentication manager
   */
  @ConditionalOnProperty(
      prefix = "bremersee.auth.password-flow",
      name = {
          "token-endpoint",
          "client-id",
          "client-secret"
      })
  @ConditionalOnBean(JwtDecoder.class)
  @ConditionalOnMissingBean(PasswordFlowAuthenticationManager.class)
  @Bean
  public PasswordFlowAuthenticationManager passwordFlowAuthenticationManager(
      ObjectProvider<JwtDecoder> jwtDecoder,
      JsonPathJwtConverter jwtConverter,
      RestTemplateAccessTokenRetriever tokenRetriever) {

    log.info("Creating application {} ...",
        PasswordFlowAuthenticationManager.class.getSimpleName());
    Assert.notNull(
        jwtDecoder.getIfAvailable(),
        "Jwt decoder must be present.");
    return new PasswordFlowAuthenticationManager(
        properties.getPasswordFlow(),
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
