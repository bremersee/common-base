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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The reactive authentication support auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass({
    JsonPathReactiveJwtConverter.class,
    WebClientAccessTokenRetriever.class
})
@EnableConfigurationProperties(AuthProperties.class)
@Configuration
@Slf4j
public class ReactiveJwtSupportAutoConfiguration {

  private final AuthProperties properties;

  /**
   * Instantiates a reactive authentication support auto configuration.
   *
   * @param properties the properties
   */
  public ReactiveJwtSupportAutoConfiguration(
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
   * Creates a json path reactive jwt converter bean.
   *
   * @return the json path reactive jwt converter
   */
  @ConditionalOnProperty(
      prefix = "spring.security.oauth2.resourceserver.jwt",
      name = "jwk-set-uri")
  @ConditionalOnMissingBean
  @Bean
  @SuppressWarnings("DuplicatedCode")
  public JsonPathReactiveJwtConverter jsonPathReactiveJwtConverter() {

    log.info("Creating application {} ...", JsonPathReactiveJwtConverter.class.getSimpleName());
    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setNameJsonPath(properties.getNameJsonPath());
    converter.setRolePrefix(properties.getRolePrefix());
    converter.setRolesJsonPath(properties.getRolesJsonPath());
    converter.setRolesValueList(properties.isRolesValueList());
    converter.setRolesValueSeparator(properties.getRolesValueSeparator());
    return new JsonPathReactiveJwtConverter(converter);
  }

  /**
   * Creates access token retriever.
   *
   * @param accessTokenCache the access token cache
   * @return the web client access token retriever
   */
  @Conditional(JwtSupportCondition.class)
  @ConditionalOnMissingBean
  @Bean
  public WebClientAccessTokenRetriever webClientAccessTokenRetriever(
      ObjectProvider<ReactiveAccessTokenCache> accessTokenCache) {

    ReactiveAccessTokenCache cache = accessTokenCache.getIfAvailable();
    log.info("Creating common {} with cache {} ...",
        WebClientAccessTokenRetriever.class.getSimpleName(), cache);
    return new WebClientAccessTokenRetriever(
        WebClient.builder().build(),
        cache);
  }

  /**
   * Creates password flow reactive authentication manager.
   *
   * @param jwtDecoder the jwt decoder
   * @param jwtConverter the jwt converter
   * @param tokenRetriever the token retriever
   * @return the password flow reactive authentication manager
   */
  @ConditionalOnProperty(
      prefix = "bremersee.auth.password-flow",
      name = {
          "token-endpoint",
          "client-id",
          "client-secret"
      })
  @ConditionalOnMissingBean
  @Bean
  public PasswordFlowReactiveAuthenticationManager passwordFlowReactiveAuthenticationManager(
      ObjectProvider<ReactiveJwtDecoder> jwtDecoder,
      JsonPathReactiveJwtConverter jwtConverter,
      WebClientAccessTokenRetriever tokenRetriever) {

    Assert.notNull(jwtDecoder.getIfAvailable(), "Jwt decoder must be present.");
    log.info("Creating {} ...", PasswordFlowReactiveAuthenticationManager.class.getSimpleName());
    return new PasswordFlowReactiveAuthenticationManager(
        properties.getPasswordFlow(),
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
