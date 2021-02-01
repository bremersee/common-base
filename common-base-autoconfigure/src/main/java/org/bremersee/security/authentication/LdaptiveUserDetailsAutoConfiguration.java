/*
 * Copyright 2021 the original author or authors.
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
import org.bremersee.data.ldaptive.LdaptiveAutoConfiguration;
import org.bremersee.data.ldaptive.LdaptiveOperations;
import org.bremersee.data.ldaptive.LdaptiveProperties;
import org.bremersee.data.ldaptive.LdaptiveProperties.UserDetailsProperties;
import org.bremersee.data.ldaptive.reactive.ReactiveLdaptiveOperations;
import org.bremersee.security.core.userdetails.LdaptivePasswordEncoder;
import org.bremersee.security.core.userdetails.LdaptivePasswordMatcher;
import org.bremersee.security.core.userdetails.LdaptiveUserDetailsService;
import org.bremersee.security.core.userdetails.ReactiveLdaptiveUserDetailsService;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The ldaptive user details auto configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnWebApplication(type = Type.ANY)
@AutoConfigureBefore(InMemoryUserDetailsAutoConfiguration.class)
@AutoConfigureAfter(LdaptiveAutoConfiguration.class)
@ConditionalOnClass({
    ConnectionFactory.class,
    LdaptiveOperations.class
})
@ConditionalOnProperty(
    prefix = "bremersee.ldaptive",
    name = {"enabled", "authentication-enabled"},
    havingValue = "true")
@EnableConfigurationProperties(LdaptiveProperties.class)
@Slf4j
public class LdaptiveUserDetailsAutoConfiguration {

  private final UserDetailsProperties properties;

  private final LdaptiveOperations ldaptiveOperations;

  /**
   * Instantiates a new ldaptive user details auto configuration.
   *
   * @param properties the properties
   * @param ldaptiveOperationsProvider the ldaptive operations provider
   */
  public LdaptiveUserDetailsAutoConfiguration(
      LdaptiveProperties properties,
      ObjectProvider<LdaptiveOperations> ldaptiveOperationsProvider) {
    this.properties = properties.getUserDetails();
    this.ldaptiveOperations = ldaptiveOperationsProvider.getIfAvailable();
    Assert.notNull(this.ldaptiveOperations, "Ldap operations must not be present.");
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
            + "* properties = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        properties);
    Assert.hasText(properties.getUserBaseDn(), "User base dn must be present.");
    Assert.hasText(properties.getUserFindOneFilter(), "User find one filter must be present.");
  }

  /**
   * Ldaptive user details service.
   *
   * @return the ldaptive user details service
   */
  @ConditionalOnWebApplication(type = Type.SERVLET)
  @ConditionalOnMissingBean(value = {UserDetailsService.class})
  @Bean
  public LdaptiveUserDetailsService ldaptiveUserDetailsService() {
    return new LdaptiveUserDetailsService(
        ldaptiveOperations,
        properties.getUserBaseDn(),
        properties.getUserFindOneFilter(),
        properties.getUserFindOneSearchScope(),
        properties.getUserAccountControlAttributeName(),
        properties.getAuthorities(),
        properties.getAuthorityAttributeName(),
        properties.isAuthorityDn(),
        properties.getAuthorityMap(),
        properties.getAuthorityPrefix());
  }

  /**
   * Reactive ldaptive user details service.
   *
   * @param reactiveLdaptiveOperationsProvider the reactive ldaptive operations provider
   * @return the reactive ldaptive user details service
   */
  @ConditionalOnWebApplication(type = Type.REACTIVE)
  @ConditionalOnMissingBean(value = {ReactiveUserDetailsService.class})
  @Bean
  public ReactiveLdaptiveUserDetailsService reactiveLdaptiveUserDetailsService(
      ObjectProvider<ReactiveLdaptiveOperations> reactiveLdaptiveOperationsProvider) {
    ReactiveLdaptiveOperations reactiveLdaptiveOperations = reactiveLdaptiveOperationsProvider.getIfAvailable();
    Assert.notNull(reactiveLdaptiveOperations, "Reactive ldap operations must not be present.");
    return new ReactiveLdaptiveUserDetailsService(
        reactiveLdaptiveOperations,
        properties.getUserBaseDn(),
        properties.getUserFindOneFilter(),
        properties.getUserFindOneSearchScope(),
        properties.getUserAccountControlAttributeName(),
        properties.getAuthorities(),
        properties.getAuthorityAttributeName(),
        properties.isAuthorityDn(),
        properties.getAuthorityMap(),
        properties.getAuthorityPrefix());
  }

  /**
   * Ldaptive password matcher.
   *
   * @return the ldaptive password matcher
   */
  @ConditionalOnMissingBean(value = {PasswordEncoder.class})
  @Bean
  public LdaptivePasswordMatcher passwordEncoder() {
    LdaptivePasswordMatcher matcher = new LdaptivePasswordMatcher(
        ldaptiveOperations,
        properties.getUserBaseDn(),
        properties.getUserFindOneFilter());
    matcher.setUserPasswordAttributeName(properties.getUserAccountControlAttributeName());
    matcher.setUserFindOneSearchScope(properties.getUserFindOneSearchScope());
    matcher.setUserPasswordAttributeName(properties.getUserPasswordAttributeName());
    matcher.setDelegate(new LdaptivePasswordEncoder(
        properties.getUserPasswordLabel(),
        properties.getUserPasswordAlgorithm()));
    return matcher;
  }

}
