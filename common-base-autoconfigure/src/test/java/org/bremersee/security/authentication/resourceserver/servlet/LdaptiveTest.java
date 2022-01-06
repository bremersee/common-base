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

package org.bremersee.security.authentication.resourceserver.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.data.ldaptive.LdaptiveOperations;
import org.bremersee.data.ldaptive.LdaptiveProperties;
import org.bremersee.security.authentication.resourceserver.servlet.withoutredis.TestConfiguration;
import org.bremersee.security.core.userdetails.LdaptiveUserDetailsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

/**
 * The ldap basic auth test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=resourceserver-ldaptive",

        "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
        "spring.ldap.embedded.credential.username=uid=admin",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=classpath:schema.ldif",
        "spring.ldap.embedded.validation.enabled=false",
        "bremersee.ldaptive.enabled=true",
        "bremersee.ldaptive.authentication-enabled=true",
        "bremersee.ldaptive.ldap-url=ldap://localhost:${spring.ldap.embedded.port}",
        "bremersee.ldaptive.use-start-tls=false",
        "bremersee.ldaptive.bind-dn=uid=admin",
        "bremersee.ldaptive.bind-credentials=secret",
        "bremersee.ldaptive.pooled=false",
        "bremersee.ldaptive.search-validator.search-request.base-dn=ou=people,dc=bremersee,dc=org",
        "bremersee.ldaptive.search-validator.search-request.search-filter.filer=uid=anna",
        "bremersee.ldaptive.search-validator.search-request.size-limit=1",
        "bremersee.ldaptive.search-validator.search-request.search-scope=ONELEVEL",

        "bremersee.ldaptive.user-details.user-base-dn=ou=people,dc=bremersee,dc=org",
        "bremersee.ldaptive.user-details.user-find-one-filter=(&(objectClass=person)(uid={0}))",
        "bremersee.ldaptive.user-details.authorities[0]=USER",
        "bremersee.ldaptive.user-details.authority-attribute-name=memberOf",
        "bremersee.ldaptive.user-details.authority-map.developers=ADMIN",
        "bremersee.ldaptive.user-details.user-password-label=",
        "bremersee.ldaptive.user-details.user-password-algorithm=",

        "bremersee.cors.enable=true",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.any-access-mode=deny_all",
        "bremersee.auth.path-matchers[0].ant-pattern=/public/**",
        "bremersee.auth.path-matchers[0].access-mode=permit_all",
        "bremersee.auth.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[1].http-method=POST",
        "bremersee.auth.path-matchers[1].roles=ROLE_ADMIN",
        "bremersee.auth.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[2].roles=ROLE_USER"
    })
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class LdaptiveTest {

  /**
   * The properties.
   */
  @Autowired
  LdaptiveProperties properties;

  /**
   * The udaptive operations.
   */
  @Autowired
  LdaptiveOperations ldaptiveOperations;

  /**
   * The user details service.
   */
  @Autowired
  LdaptiveUserDetailsService userDetailsService;

  /**
   * The Test rest template.
   */
  @Autowired
  TestRestTemplate testRestTemplate;

  /**
   * The Rest template builder.
   */
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * The user password.
   */
  String userPassword;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Rest template rest template.
   *
   * @return the rest template
   */
  RestTemplate restTemplate() {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .build();
  }

  /**
   * Rest template rest template.
   *
   * @param user the user
   * @param password the password
   * @return the rest template
   */
  RestTemplate restTemplate(@SuppressWarnings("SameParameterValue") String user, String password) {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .basicAuthentication(user, password)
        .build();
  }

  /**
   * Sets embedded ldap port.
   */
  @BeforeAll
  static void setEmbeddedLdapPort() {
    int embeddedLdapPort = SocketUtils.findAvailableTcpPort(10000);
    System.setProperty("spring.ldap.embedded.port", String.valueOf(embeddedLdapPort));
  }

  /**
   * Setup tests.
   */
  @BeforeEach
  void setUp() {
    userPassword = ldaptiveOperations.generateUserPassword("uid=anna," + properties.getUserDetails().getUserBaseDn());
  }

  /**
   * Find user details.
   */
  @Test
  void findUserDetails() {
    UserDetails userDetails = userDetailsService.loadUserByUsername("anna");
    assertNotNull(userDetails);
    assertEquals("anna", userDetails.getUsername());
    assertEquals("anna", userDetails.getPassword());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  /**
   * Gets public.
   */
  @Test
  void getPublic() {
    assertEquals(
        "public",
        restTemplate()
            .getForEntity("/public", String.class)
            .getBody());
  }

  /**
   * Gets protected.
   */
  @Test
  void getProtected() {
    ResponseEntity<String> response = restTemplate("anna", userPassword)
        .getForEntity("/protected", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("protected", response.getBody());
  }

  /**
   * Gets protected and expect unauthorized.
   */
  @Test
  void getProtectedAndExpectUnauthorized() {
    // We use the test rest template here, because the real rest template will throw an exception
    ResponseEntity<String> response = testRestTemplate
        .withBasicAuth("anna", "someone")
        .getForEntity("/protected", String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Post protected.
   */
  @Test
  void postProtected() {
    ResponseEntity<String> response = restTemplate("anna", userPassword)
        .postForEntity("/protected", "hello", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("hello", response.getBody());
  }

}
