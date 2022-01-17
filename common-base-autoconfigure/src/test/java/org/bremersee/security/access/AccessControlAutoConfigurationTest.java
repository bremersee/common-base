/*
 * Copyright 2020-2022 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bremersee.common.model.AccessControlList;
import org.bremersee.converter.ModelMapperConfigurerAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The access control autoconfiguration test.
 *
 * @author Christian Bremer
 */
class AccessControlAutoConfigurationTest {

  /**
   * Acl mapper.
   */
  @Test
  void aclMapper() {
    AccessControlProperties properties = new AccessControlProperties();
    AccessControlAutoConfiguration configuration = new AccessControlAutoConfiguration(properties);
    configuration.init();

    AclFactory<AclImpl> aclFactory = AclImpl::new;
    AclMapper<AclImpl> aclMapper = configuration.aclMapper(objectProvider(aclFactory));
    assertNotNull(aclMapper);
    assertNotNull(aclMapper.getAclFactory());
    assertEquals(AclImpl.class, aclMapper.getAclFactory().getAccessControlListClass());
  }

  /**
   * Acl model mapper config adapter.
   */
  @Test
  void aclModelMapperConfigAdapter() {
    AccessControlProperties properties = new AccessControlProperties();
    properties.setSwitchAdminAccess(false);
    properties.setDefaultPermissions(Collections.emptySet());
    AccessControlAutoConfiguration configuration = new AccessControlAutoConfiguration(properties);
    configuration.init();

    AclFactory<AclEntity> aclFactory = AclEntity::new;
    AclMapper<AclEntity> aclMapper = configuration.aclMapper(objectProvider(aclFactory));
    ModelMapperConfigurerAdapter adapter = configuration
        .aclModelMapperConfigAdapter(objectProvider(aclMapper));
    assertNotNull(adapter);

    ModelMapper modelMapper = new ModelMapper();
    adapter.configure(modelMapper);

    // test from dto to entity
    AccessControlList dto = AclBuilder.builder()
        .owner("anna")
        .guest(true, PermissionConstants.READ)
        .addUser("otto", PermissionConstants.WRITE)
        .buildAccessControlList();

    AclEntity entity = modelMapper.map(dto, AclEntity.class);

    assertNotNull(entity);
    assertEquals("anna", entity.getOwner());

    assertNotNull(entity.get(PermissionConstants.READ));
    Ace readAce = entity.get(PermissionConstants.READ);
    assertTrue(readAce.isGuest());

    Ace writeAce = entity.get(PermissionConstants.WRITE);
    assertTrue(writeAce.getUsers().contains("otto"));

    // test from entity to dto
    AccessControlList actualDto = modelMapper.map(entity, AccessControlList.class);
    // AccessControlList actualDto = aclMapper.map(entity);
    assertNotNull(actualDto);
    assertEquals(dto, actualDto);

    testHolders(modelMapper);
  }

  private void testHolders(ModelMapper modelMapper) {
    AclHolder aclHolder = new AclHolder();
    aclHolder.setId("123");
    aclHolder.setOrder(100);
    aclHolder.setName("test");
    aclHolder.setTranslations(Collections.singleton("item"));
    aclHolder.setAcl(AclBuilder.builder()
        .owner("anna")
        .guest(true, PermissionConstants.READ)
        .addUser("otto", PermissionConstants.WRITE)
        .buildAccessControlList());

    AclEntityHolder aclEntityHolder = modelMapper.map(aclHolder, AclEntityHolder.class);
    assertNotNull(aclEntityHolder.getAcl());
    assertEquals("anna", aclEntityHolder.getAcl().getOwner());

    AclHolder newAclHolder = modelMapper.map(aclEntityHolder, AclHolder.class);
    assertNotNull(newAclHolder.getAcl());
  }

  private static <T> ObjectProvider<T> objectProvider(T provides) {
    //noinspection unchecked
    ObjectProvider<T> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(provides);
    //noinspection unchecked
    doAnswer((Answer<Void>) invocation -> {
      invocation.callRealMethod();
      return null;
    }).when(provider).ifAvailable(any(Consumer.class));
    return provider;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  private static class AclHolder {

    private String id;

    private AccessControlList acl;

    private int order;

    private String name;

    private Set<String> translations = new LinkedHashSet<>();

    /**
     * Is acl present boolean.
     *
     * @return the boolean
     */
    @SuppressWarnings("unused")
    public boolean isAclPresent() {
      return getAcl() != null;
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  private static class AclEntityHolder {

    private String id;

    private AclEntity acl;

    private int order;

    private String name;

    private Set<String> translations = new LinkedHashSet<>();

    /**
     * Is acl present boolean.
     *
     * @return the boolean
     */
    @SuppressWarnings("unused")
    public boolean isAclPresent() {
      return getAcl() != null;
    }
  }
}