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

package org.bremersee.security.authentication.resourceserver.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.bremersee.data.ldaptive.LdaptiveOperations;
import org.bremersee.data.ldaptive.LdaptiveProperties;
import org.bremersee.security.authentication.resourceserver.reactive.withoutredis.TestConfiguration;
import org.bremersee.security.core.userdetails.ReactiveLdaptiveUserDetailsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.SocketUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The reactive ldap basic auth test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
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
public class ReactiveLdaptiveTest {

  /**
   * The properties.
   */
  @Autowired
  LdaptiveProperties properties;

  /**
   * The ldaptive operations.
   */
  @Autowired
  LdaptiveOperations ldaptiveOperations;

  /**
   * The user details service.
   */
  @Autowired
  ReactiveLdaptiveUserDetailsService userDetailsService;

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Creates a new web client, that uses the real security configuration.
   *
   * @return the web client
   */
  WebClient newWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl())
        .build();
  }

  /**
   * The user password.
   */
  String userPassword;

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
    StepVerifier.create(userDetailsService.findByUsername("anna"))
        .assertNext(userDetails -> {
          assertEquals("anna", userDetails.getUsername());
          assertEquals("anna", userDetails.getPassword());
          assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
          assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        })
        .verifyComplete();
  }

  /**
   * Gets public.
   */
  @Test
  void getPublic() {
    StepVerifier.create(newWebClient()
            .get()
            .uri("/public")
            .retrieve()
            .bodyToMono(String.class))
        .assertNext(body -> assertEquals("public", body))
        .verifyComplete();
  }

  /**
   * Gets protected.
   */
  @Test
  void getProtected() {
    StepVerifier.create(newWebClient()
            .get()
            .uri("/protected")
            .headers(httpHeaders -> httpHeaders
                .setBasicAuth("anna", userPassword, StandardCharsets.UTF_8))
            .retrieve()
            .bodyToMono(String.class))
        .assertNext(body -> assertEquals("protected", body))
        .verifyComplete();
  }

  /**
   * Gets protected and expect unauthorized.
   */
  @Test
  void getProtectedAndExpectUnauthorized() {
    StepVerifier.create(newWebClient()
            .get()
            .uri("/protected")
            .headers(httpHeaders -> httpHeaders
                .setBasicAuth("anna", "someone", StandardCharsets.UTF_8))
            .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode())))
        .assertNext(statusCode -> assertEquals(HttpStatus.UNAUTHORIZED, statusCode))
        .verifyComplete();
  }

  /**
   * Post protected.
   */
  @Test
  void postProtected() {
    StepVerifier.create(newWebClient()
            .post()
            .uri("/protected")
            .contentType(MediaType.TEXT_PLAIN)
            .accept(MediaType.TEXT_PLAIN)
            .headers(httpHeaders -> httpHeaders
                .setBasicAuth("anna", userPassword, StandardCharsets.UTF_8))
            .body(BodyInserters.fromValue("hello"))
            .retrieve()
            .bodyToMono(String.class))
        .assertNext(body -> assertEquals("hello", body))
        .verifyComplete();
  }

}
