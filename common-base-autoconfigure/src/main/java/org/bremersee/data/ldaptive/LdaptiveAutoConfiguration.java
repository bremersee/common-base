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
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.provider.Provider;
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

  private final LdaptiveConnectionPoolFactory connectionPoolFactory;

  private final Provider<?> ldaptiveProvider;

  /**
   * Instantiates a new ldaptive configuration.
   *
   * @param ldaptiveProperties the ldaptive properties
   * @param connectionConfigFactory the connection config factory
   * @param connectionPoolFactory the connection pool factory
   * @param ldaptiveProvider the ldaptive provider
   */
  public LdaptiveAutoConfiguration(
      LdaptiveProperties ldaptiveProperties,
      ObjectProvider<LdaptiveConnectionConfigFactory> connectionConfigFactory,
      ObjectProvider<LdaptiveConnectionPoolFactory> connectionPoolFactory,
      ObjectProvider<Provider<?>> ldaptiveProvider) {
    this.properties = ldaptiveProperties;
    this.connectionConfigFactory = connectionConfigFactory
        .getIfAvailable(LdaptiveConnectionConfigFactory::defaultFactory);
    this.connectionPoolFactory = connectionPoolFactory
        .getIfAvailable(LdaptiveConnectionPoolFactory::defaultFactory);
    this.ldaptiveProvider = ldaptiveProvider.getIfAvailable();
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
            + "* connectionPoolFactory = {}\n"
            + "* ldaptiveProvider = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        ClassUtils.getUserClass(connectionConfigFactory).getSimpleName(),
        ClassUtils.getUserClass(connectionConfigFactory).getSimpleName(),
        ldaptiveProvider != null
            ? ClassUtils.getUserClass(ldaptiveProvider).getSimpleName()
            : "null");

    if (properties.isPooled()) {
      LdaptiveTemplate template = new LdaptiveTemplate(defaultConnectionFactory());
      boolean exists = template
          .findOne(properties.getSearchValidator().getSearchRequest())
          .isPresent();
      if (!exists) {
        ServiceException se = ServiceException.internalServerError(
            "Invalid search validator. There is no result executing search validator.",
            "org.bremersee:common-base-ldaptive-autoconfigure:"
                + "bf2c08f6-65bf-417c-8ab9-1c069f46bde2");
        log.error("Validation of pool validation failed.", se);
        throw se;
      }
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
  @Bean
  public ConnectionFactory connectionFactory() {
    return properties.isPooled() ? pooledConnectionFactory() : defaultConnectionFactory();
  }

  private DefaultConnectionFactory defaultConnectionFactory() {
    DefaultConnectionFactory factory = new DefaultConnectionFactory();
    factory.setConnectionConfig(connectionConfig());
    if (ldaptiveProvider != null) {
      factory.setProvider(ldaptiveProvider);
    }
    return factory;
  }

  private PooledConnectionFactory pooledConnectionFactory() {
    PooledConnectionFactory factory = new PooledConnectionFactory();
    factory.setConnectionPool(connectionPool());
    return factory;
  }

  private ConnectionConfig connectionConfig() {
    return connectionConfigFactory.createConnectionConfig(properties);
  }

  private ConnectionPool connectionPool() {
    ConnectionPool pool = connectionPoolFactory.createConnectionPool(
        properties,
        defaultConnectionFactory());
    pool.initialize();
    return pool;
  }

}
