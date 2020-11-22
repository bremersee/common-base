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

package org.bremersee.data.ldaptive;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.pool.IdlePruneStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The ldaptive configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass({
    DefaultConnectionFactory.class,
    LdaptiveTemplate.class
})
@ConditionalOnProperty(prefix = "bremersee.ldaptive", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(LdaptiveProperties.class)
@Slf4j
public class LdaptiveAutoConfiguration {

  private final LdaptiveProperties properties;

  private final LdaptiveConnectionConfigFactory connectionConfigFactory;

  /**
   * Instantiates a new ldaptive configuration.
   *
   * @param ldaptiveProperties the ldaptive properties
   * @param connectionConfigFactory the connection config factory
   */
  public LdaptiveAutoConfiguration(
      LdaptiveProperties ldaptiveProperties,
      ObjectProvider<LdaptiveConnectionConfigFactory> connectionConfigFactory) {
    this.properties = ldaptiveProperties;
    this.connectionConfigFactory = connectionConfigFactory
        .getIfAvailable(LdaptiveConnectionConfigFactory::defaultFactory);
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
            + "* connectionConfigFactory = {}\n"
            + "* properties = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        ClassUtils.getUserClass(connectionConfigFactory).getSimpleName(),
        properties);

    if (properties.isPooled()) {
      log.info("Checking validation properties {}", properties.getSearchValidator());
      LdaptiveTemplate ldaptiveTemplate = new LdaptiveTemplate(defaultConnectionFactory());
      SearchRequest request = properties.getSearchValidator().getSearchRequest().createSearchRequest();
      SearchResponse response = ldaptiveTemplate.search(request);
      if (!response.isSuccess()) {
        ServiceException se = ServiceException.internalServerError(
            "Invalid search validator. There is no result executing search validator.",
            "org.bremersee:common-base-ldaptive-autoconfigure:"
                + "bf2c08f6-65bf-417c-8ab9-1c069f46bde2");
        log.error("Validation of pool validation failed.", se);
        throw se;
      }
      log.info("Checking validation properties: successfully done!");
    }
  }

  /**
   * Builds ldaptive template.
   *
   * @param connectionFactory the connection factory
   * @return the ldaptive template
   */
  @Bean
  public LdaptiveTemplate ldaptiveTemplate(ConnectionFactory connectionFactory) {
    return new LdaptiveTemplate(connectionFactory);
  }

  /**
   * Builds connection factory bean.
   *
   * @return the connection factory bean
   */
  @Bean(destroyMethod = "close")
  public ConnectionFactory connectionFactory() {
    return properties.isPooled() ? pooledConnectionFactory() : defaultConnectionFactory();
  }

  private DefaultConnectionFactory defaultConnectionFactory() {
    return DefaultConnectionFactory.builder()
        .config(connectionConfigFactory.createConnectionConfig(properties))
        .build();
  }

  private PooledConnectionFactory pooledConnectionFactory() {
    PooledConnectionFactory factory = PooledConnectionFactory.builder()
        .config(connectionConfigFactory.createConnectionConfig(properties))
        .blockWaitTime(properties.getBlockWaitTime())
        .connectOnCreate(properties.isConnectOnCreate())
        .failFastInitialize(properties.isFailFastInitialize())
        .max(properties.getMaxPoolSize())
        .min(properties.getMinPoolSize())
        .pruneStrategy(new IdlePruneStrategy(properties.getPrunePeriod(), properties.getIdleTime()))
        .validateOnCheckIn(properties.isValidateOnCheckIn())
        .validateOnCheckOut(properties.isValidateOnCheckOut())
        .validatePeriodically(properties.isValidatePeriodically())
        .validator(properties.createSearchConnectionValidator())
        .build();
    factory.initialize();
    return factory;
  }

}
