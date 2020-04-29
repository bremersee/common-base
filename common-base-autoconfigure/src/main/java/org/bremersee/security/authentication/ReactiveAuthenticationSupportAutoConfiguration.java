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
import org.bremersee.security.SecurityProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The reactive authentication support auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "bremersee.security.authentication",
    name = "enable-jwt-support",
    havingValue = "true")
@ConditionalOnClass({
    JsonPathReactiveJwtConverter.class,
    WebClientAccessTokenRetriever.class
})
@EnableConfigurationProperties(SecurityProperties.class)
@Configuration
@Slf4j
public class ReactiveAuthenticationSupportAutoConfiguration {

  private SecurityProperties properties;

  /**
   * Instantiates a reactive authentication support auto configuration.
   *
   * @param properties the properties
   */
  public ReactiveAuthenticationSupportAutoConfiguration(
      SecurityProperties properties) {
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
        properties.getAuthentication().getRolesJsonPath(),
        properties.getAuthentication().isRolesValueList(),
        properties.getAuthentication().getRolesValueSeparator(),
        properties.getAuthentication().getRolePrefix(),
        properties.getAuthentication().getNameJsonPath());
  }

  /**
   * Json path reactive jwt converter json path reactive jwt converter.
   *
   * @return the json path reactive jwt converter
   */
  @ConditionalOnMissingBean
  @Bean
  @SuppressWarnings("DuplicatedCode")
  public JsonPathReactiveJwtConverter jsonPathReactiveJwtConverter() {
    log.info("Creating {} ...", JsonPathReactiveJwtConverter.class.getSimpleName());
    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setNameJsonPath(properties.getAuthentication().getNameJsonPath());
    converter.setRolePrefix(properties.getAuthentication().getRolePrefix());
    converter.setRolesJsonPath(properties.getAuthentication().getRolesJsonPath());
    converter.setRolesValueList(properties.getAuthentication().isRolesValueList());
    converter.setRolesValueSeparator(properties.getAuthentication().getRolesValueSeparator());
    return new JsonPathReactiveJwtConverter(converter);
  }

  /**
   * Web client access token retriever web client access token retriever.
   *
   * @return the web client access token retriever
   */
  @ConditionalOnMissingBean
  @Bean
  public WebClientAccessTokenRetriever webClientAccessTokenRetriever() {
    log.info("Creating {} ...", WebClientAccessTokenRetriever.class.getSimpleName());
    return new WebClientAccessTokenRetriever();
  }

  /**
   * Password flow reactive authentication manager password flow reactive authentication manager.
   *
   * @param jwtDecoder the jwt decoder
   * @param jwtConverter the jwt converter
   * @param tokenRetriever the token retriever
   * @return the password flow reactive authentication manager
   */
  @ConditionalOnMissingBean
  @Bean
  public PasswordFlowReactiveAuthenticationManager passwordFlowReactiveAuthenticationManager(
      ObjectProvider<ReactiveJwtDecoder> jwtDecoder,
      JsonPathReactiveJwtConverter jwtConverter,
      WebClientAccessTokenRetriever tokenRetriever) {

    Assert.notNull(jwtDecoder.getIfAvailable(), "Jwt decoder must be present.");
    log.info("Creating {} ...", PasswordFlowReactiveAuthenticationManager.class.getSimpleName());
    return new PasswordFlowReactiveAuthenticationManager(
        properties.getAuthentication().getPasswordFlow(),
        jwtDecoder.getIfAvailable(),
        jwtConverter,
        tokenRetriever);
  }

}
