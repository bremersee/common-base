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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.LdaptiveTemplate;
import org.bremersee.data.ldaptive.reactive.ReactiveLdaptiveTemplate;
import org.bremersee.data.ldaptive.transcoder.UserAccountControlValueTranscoder;
import org.bremersee.security.core.userdetails.app.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.test.StepVerifier;

/**
 * The ldaptive authentication test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = {TestConfiguration.class},
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "security.basic.enabled=false",
        "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
        "spring.ldap.embedded.credential.username=uid=admin",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=classpath:schema.ldif",
        "spring.ldap.embedded.port=17389",
        "spring.ldap.embedded.validation.enabled=false"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
@Slf4j
class LdaptiveAuthenticationTest {

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  @Autowired
  private LdaptiveTemplate ldaptiveTemplate;

  @Autowired
  private ReactiveLdaptiveTemplate reactiveLdaptiveTemplate;

  private LdaptiveUserDetailsService userDetailsService;

  private ReactiveLdaptiveUserDetailsService reactiveUserDetailsService;

  private LdaptivePasswordMatcher passwordMatcher;

  private String annaPassword;

  /**
   * Sets up.
   */
  @BeforeAll
  void setUp() {
    String userBaseDn = "ou=people," + baseDn;
    String userFindOneFilter = "(&(objectClass=person)(uid={0}))";
    userDetailsService = new LdaptiveUserDetailsService(
        ldaptiveTemplate,
        userBaseDn,
        userFindOneFilter,
        SearchScope.ONELEVEL,
        UserAccountControlValueTranscoder.ATTRIBUTE_NAME,
        Collections.singletonList("USERS"),
        "memberOf",
        true,
        null,
        "ROLE_");
    reactiveUserDetailsService = new ReactiveLdaptiveUserDetailsService(
        reactiveLdaptiveTemplate,
        userBaseDn,
        userFindOneFilter,
        SearchScope.ONELEVEL,
        null,
        Collections.singletonList("ROLE_USERS"),
        "memberOf",
        true,
        null,
        "ROLE_");
    passwordMatcher = new LdaptivePasswordMatcher(
        ldaptiveTemplate,
        userBaseDn,
        userFindOneFilter);
    passwordMatcher.setDelegate(LdaptivePasswordEncoder.plainWithNoLabel());
    passwordMatcher.setUserFindOneSearchScope(SearchScope.ONELEVEL);
    passwordMatcher.setUserPasswordAttributeName("userPassword");
    annaPassword = ldaptiveTemplate.generateUserPassword("uid=anna," + userBaseDn);

    LdapEntry entry = ldaptiveTemplate.findOne(SearchRequest.builder()
        .dn(userBaseDn)
        .filter(FilterTemplate.builder()
            .filter(userFindOneFilter)
            .parameters("anna")
            .build())
        .build())
        .orElse(null);
    assertNotNull(entry);
  }

  /**
   * Authenticate user.
   */
  @Test
  void authenticateUser() {
    assertNotNull(annaPassword, "The password of anna must not be null.");
    UserDetails userDetails = userDetailsService.loadUserByUsername("anna");
    assertEquals("anna", userDetails.getUsername());
    assertEquals("anna", userDetails.getPassword());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USERS")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_developers")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_managers")));
    assertFalse(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_admins")));
    assertNotNull(userDetails);
    assertTrue(passwordMatcher.matches(annaPassword, userDetails.getPassword()));
  }

  /**
   * Reactive authenticate user.
   */
  @Test
  void reactiveAuthenticateUser() {
    UserDetailsRepositoryReactiveAuthenticationManager manager
        = new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService);
    manager.setPasswordEncoder(passwordMatcher);

    Authentication authentication = new UsernamePasswordAuthenticationToken("anna", annaPassword);
    StepVerifier.create(manager.authenticate(authentication))
        .assertNext(auth -> {
          assertTrue(auth.isAuthenticated());
          assertEquals("anna", auth.getName());
          assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USERS")));
          assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_developers")));
          assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_managers")));
          assertFalse(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_admins")));
        })
        .verifyComplete();
  }

  /**
   * Matches no user name.
   */
  @Test
  void matchesNoUserName() {
    assertFalse(passwordMatcher.matches("", null));
  }

  /**
   * Test to string.
   */
  @Test
  void testToString() {
    assertTrue(userDetailsService.toString().contains(baseDn));
    assertTrue(reactiveUserDetailsService.toString().contains(baseDn));
  }
}