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

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * The jwt authentication token security context factory.
 *
 * @author Christian Bremer
 */
public class JwtAuthenticationTokenSecurityContextFactory
    implements WithSecurityContextFactory<WithJwtAuthenticationToken> {

  @Override
  public SecurityContext createSecurityContext(
      WithJwtAuthenticationToken withJwtAuthenticationToken) {

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication authentication = new JwtAuthenticationToken(
        createSpringJwt(createJwt(withJwtAuthenticationToken)));
    context.setAuthentication(authentication);
    return context;
  }

  private JWT createJwt(WithJwtAuthenticationToken withJwtAuthenticationToken) {
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .audience(withJwtAuthenticationToken.audience())
        .expirationTime(new Date(System.currentTimeMillis()
            + withJwtAuthenticationToken.addMillisToExpirationTime()))
        .issuer(withJwtAuthenticationToken.issuer())
        .issueTime(new Date(System.currentTimeMillis()
            + withJwtAuthenticationToken.addMillisToIssueTime()))
        .jwtID(withJwtAuthenticationToken.jwtId())
        .notBeforeTime(new Date(System.currentTimeMillis()
            + withJwtAuthenticationToken.addMillisToNotBeforeTime()))
        .subject(withJwtAuthenticationToken.subject())
        .build();
    return new PlainJWT(claimsSet);
  }

  private Jwt createSpringJwt(JWT jwt) {
    try {
      String tokenValue = jwt.serialize();
      Instant issuedAt = jwt.getJWTClaimsSet().getIssueTime().toInstant();
      Instant expiresAt = jwt.getJWTClaimsSet().getExpirationTime().toInstant();
      Map<String, Object> headers = jwt.getHeader().toJSONObject();
      Map<String, Object> claims = jwt.getJWTClaimsSet().toJSONObject();
      return new Jwt(tokenValue, issuedAt, expiresAt, headers, claims);
    } catch (Exception e) {
      throw new RuntimeException("Creating Spring Jwt failed.", e);
    }
  }
}
