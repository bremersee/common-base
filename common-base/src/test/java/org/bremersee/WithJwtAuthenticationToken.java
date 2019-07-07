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

package org.bremersee;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * The annotation with jwt authentication token.
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
   * Add millis to expiration time long.
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
   * Add millis to issue time long.
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

}
