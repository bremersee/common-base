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

package org.bremersee.security.core.userdetails;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bremersee.data.ldaptive.transcoder.UserAccountControlValueTranscoder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The user details mapper test.
 *
 * @author Christian Bremer
 */
class UserDetailsLdapMapperTest {

  private static UserDetailsLdapMapper mapper;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    Map<String, String> authorityMap = new HashMap<>();
    authorityMap.put("managers", "superusers");
    mapper = new UserDetailsLdapMapper(
        "anna",
        UserAccountControlValueTranscoder.ATTRIBUTE_NAME,
        Collections.singletonList("users"),
        "memberOf",
        false,
        authorityMap,
        null);
  }

  /**
   * Gets object classes.
   */
  @Test
  void getObjectClasses() {
    assertNull(mapper.getObjectClasses());
  }

  /**
   * Map dn.
   */
  @Test
  void mapDn() {
    assertNull(mapper.mapDn(mock(UserDetails.class)));
  }

  /**
   * Map.
   */
  @Test
  void map() {
    LdapEntry ldapEntry = LdapEntry.builder()
        .dn("uid=anna,ou=people,dc=localhost")
        .attributes(LdapAttribute.builder()
            .name("memberOf")
            .binary(false)
            .values("developers", "managers")
            .build())
        .build();
    UserDetails userDetails = mapper.map(ldapEntry);
    assertNotNull(userDetails);
    assertEquals("anna", userDetails.getUsername());
    assertEquals("anna", userDetails.getPassword());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("users")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("developers")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("superusers")));
    assertFalse(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("admins")));
  }

  /**
   * Map with no granted authorities.
   */
  @Test
  void mapWithNoGrantedAuthorities() {
    UserDetailsLdapMapper otherMapper = new UserDetailsLdapMapper(
        "anna",
        null,
        null,
        null,
        false,
        null,
        null);
    LdapEntry ldapEntry = LdapEntry.builder()
        .dn("uid=anna,ou=people,dc=localhost")
        .attributes(LdapAttribute.builder()
            .name("memberOf")
            .binary(false)
            .values("developers", "managers")
            .build())
        .build();
    UserDetails userDetails = otherMapper.map(ldapEntry);
    assertNotNull(userDetails);
    assertEquals("anna", userDetails.getUsername());
    assertEquals("anna", userDetails.getPassword());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.getAuthorities().isEmpty());
  }

  /**
   * Map and expect disabled.
   */
  @Test
  void mapAndExpectDisabled() {
    LdapEntry ldapEntry = LdapEntry.builder()
        .dn("uid=anna,ou=people,dc=localhost")
        .attributes(LdapAttribute.builder()
            .name(UserAccountControlValueTranscoder.ATTRIBUTE_NAME)
            .binary(false)
            .values(String.valueOf(UserAccountControlValueTranscoder.getUserAccountControlValue(false, null)))
            .build())
        .build();
    UserDetails userDetails = mapper.map(ldapEntry);
    assertNotNull(userDetails);
    assertEquals("anna", userDetails.getUsername());
    assertEquals("anna", userDetails.getPassword());
    assertFalse(userDetails.isEnabled());
  }

  /**
   * Map and expect unsupported operation.
   */
  @Test
  void mapAndExpectUnsupportedOperation() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.map(mock(LdapEntry.class), mock(UserDetails.class)));
  }

  /**
   * Map and compute modifications.
   */
  @Test
  void mapAndComputeModifications() {
    assertArrayEquals(
        new AttributeModification[0],
        mapper.mapAndComputeModifications(mock(UserDetails.class), mock(LdapEntry.class)));
  }

  /**
   * Test to string.
   */
  @Test
  void testToString() {
    String str = mapper.toString();
    assertNotNull(str);
    assertTrue(str.contains("anna"));
  }

  /**
   * Gets type.
   */
  @Test
  void getType() {
    assertNotNull(mapper.getAuthorityTranscoder().getType());
  }

  /**
   * Encode string value.
   */
  @Test
  void encodeStringValue() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> mapper.getAuthorityTranscoder().encodeStringValue(mock(GrantedAuthority.class)));

  }

}