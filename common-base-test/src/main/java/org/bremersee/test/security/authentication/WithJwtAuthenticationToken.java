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

package org.bremersee.test.security.authentication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * The interface With jwt authentication token.
 *
 * @author Christian Bremer
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = JwtAuthenticationTokenSecurityContextFactory.class)
public @interface WithJwtAuthenticationToken {

  /**
   * Audience.
   *
   * @return the audience
   */
  String audience() default "http://audience";

  /**
   * Add millis to expiration time.
   *
   * @return the millis
   */
  long addMillisToExpirationTime() default 1000L * 60L * 30L;

  /**
   * Issuer.
   *
   * @return the issuer
   */
  String issuer() default "http://issuer";

  /**
   * Add millis to issue time.
   *
   * @return the millis
   */
  long addMillisToIssueTime() default 0L;

  /**
   * Jwt id.
   *
   * @return the jwt id
   */
  String jwtId() default "080836fb-7e74-4a56-92ba-08aeaf9a3851";

  /**
   * Add millis to not before time (should be negative).
   *
   * @return the millis
   */
  long addMillisToNotBeforeTime() default -1000L;

  /**
   * Subject.
   *
   * @return the subject
   */
  String subject() default "1918e152-294b-4701-a2c8-b9090bb5aa07";

  /**
   * The full name of the principal.
   *
   * @return the name
   */
  String name() default "Anna Livia Plurabelle";

  /**
   * Name path.
   *
   * @return the path
   */
  String namePath() default "name";

  /**
   * Preferred user name.
   *
   * @return the preferred user name
   */
  String preferredUsername() default "anna";

  /**
   * Preferred user name path.
   *
   * @return the path
   */
  String preferredUsernamePath() default "preferred_username";

  /**
   * Given name.
   *
   * @return the given name
   */
  String givenName() default "Anna Livia";

  /**
   * Given name path.
   *
   * @return the path
   */
  String givenNamePath() default "given_name";

  /**
   * Family name.
   *
   * @return the family name
   */
  String familyName() default "Plurabelle";

  /**
   * Family name path.
   *
   * @return the path
   */
  String familyNamePath() default "family_name";

  /**
   * Email.
   *
   * @return the email
   */
  String email() default "anna.livia.plurabelle@example.net";

  /**
   * Email path.
   *
   * @return the path
   */
  String emailPath() default "email";

  /**
   * Scopes.
   *
   * @return the scopes
   */
  String[] scope() default "";

  /**
   * Scope path.
   *
   * @return the path
   */
  String scopePath() default "scope";

  /**
   * Roles (granted authorities).
   *
   * @return the roles
   */
  String[] roles() default "";

  /**
   * Roles path.
   *
   * @return the path
   */
  String rolesPath() default "realm_access.roles";

  /**
   * Json path jwt convert properties.
   *
   * @return the json path jwt convert properties
   */
  JsonPathJwtConverterProperties jwtConverter() default @JsonPathJwtConverterProperties;

}
