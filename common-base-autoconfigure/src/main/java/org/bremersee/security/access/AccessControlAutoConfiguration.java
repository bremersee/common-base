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

package org.bremersee.security.access;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.converter.ModelMapperConfigurerAdapter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The access control auto configuration.
 *
 * @author Christian Bremer
 */
@EnableConfigurationProperties(AccessControlProperties.class)
@ConditionalOnBean(AclFactory.class)
@Configuration
@Slf4j
public class AccessControlAutoConfiguration {

  /**
   * The properties.
   */
  AccessControlProperties properties;

  /**
   * Instantiates a new access control auto configuration.
   *
   * @param properties the properties
   */
  public AccessControlAutoConfiguration(AccessControlProperties properties) {
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
            + "* defaultPermissions = {}\n"
            + "* adminRoles = {}\n"
            + "* switchAdminAccess = {}\n"
            + "* returnNull = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        properties.getDefaultPermissions(),
        properties.getAdminRoles(),
        properties.isSwitchAdminAccess(),
        properties.isReturnNull());
  }

  /**
   * Creates acl mapper bean.
   *
   * @param <T> the acl entity type
   * @param aclFactoryProvider the acl factory provider
   * @return the acl mapper
   */
  @Bean
  public <T extends Acl<? extends Ace>> AclMapper<T> aclMapper(
      ObjectProvider<AclFactory<T>> aclFactoryProvider) {

    AclFactory<T> aclFactory = aclFactoryProvider.getIfAvailable();
    Assert.notNull(aclFactory, "Acl factory must be present.");
    log.info("Creating bean 'aclMapper' for acl entity '{}' ...",
        ClassUtils.getUserClass(aclFactory.getAccessControlListClass()));
    String[] defaultPermissions = properties.getDefaultPermissions().toArray(new String[0]);
    AclMapperImpl<T> aclMapper = new AclMapperImpl<>(
        aclFactory,
        defaultPermissions,
        properties.isSwitchAdminAccess(),
        properties.isReturnNull());
    aclMapper.setAdminRoles(properties.getAdminRoles());
    return aclMapper;
  }

  /**
   * Creates a model mapper configurer adapter for the acl entity of the acl factory.
   *
   * @param <T> the acl entity type
   * @param aclMapperProvider the acl mapper provider
   * @return the model mapper configurer adapter
   */
  @ConditionalOnClass(ModelMapper.class)
  @Bean(name = "aclModelMapperConfigurerAdapter")
  public <T extends Acl<? extends Ace>> ModelMapperConfigurerAdapter aclModelMapperConfigAdapter(
      ObjectProvider<AclMapper<T>> aclMapperProvider) {

    log.info("Creating bean 'aclModelMapperConfigurerAdapter' ...");
    return modelMapper -> aclMapperProvider.ifAvailable(aclMapper -> {
      Class<T> aclEntityClass = aclMapper.getAclFactory()
          .getAccessControlListClass();
      log.info("Model mapper for acl is using entity class '{}'.", aclEntityClass.getName());
      modelMapper
          .createTypeMap(AccessControlList.class, aclEntityClass)
          .setConverter(context -> aclMapper.map(context.getSource()));
      modelMapper
          .createTypeMap(aclEntityClass, AccessControlList.class)
          .setConverter(context -> aclMapper.map(context.getSource()));
    });
  }

}
