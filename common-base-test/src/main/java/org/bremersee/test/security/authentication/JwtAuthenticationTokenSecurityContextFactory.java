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

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.bremersee.security.authentication.KeycloakJwtConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.StringUtils;

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
    Authentication authentication = createJwtConverter(withJwtAuthenticationToken)
        .convert(createSpringJwt(createJwt(withJwtAuthenticationToken)));
    context.setAuthentication(authentication);
    return context;
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> createJwtConverter(
      WithJwtAuthenticationToken withJwtAuthenticationToken) {
    try {
      return withJwtAuthenticationToken.jwtConverterFactory().newInstance().createJwtConverter();
    } catch (Exception e) {
      return new KeycloakJwtConverter();
    }
  }

  private JWT createJwt(WithJwtAuthenticationToken tokenValues) {
    JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
        .audience(tokenValues.audience())
        .expirationTime(new Date(System.currentTimeMillis()
            + tokenValues.addMillisToExpirationTime()))
        .issuer(tokenValues.issuer())
        .issueTime(new Date(System.currentTimeMillis()
            + tokenValues.addMillisToIssueTime()))
        .jwtID(tokenValues.jwtId())
        .notBeforeTime(new Date(System.currentTimeMillis()
            + tokenValues.addMillisToNotBeforeTime()))
        .subject(tokenValues.subject());
    for (Map.Entry<String, Object> entry : createAdditionalClaims(tokenValues).entrySet()) {
      builder.claim(entry.getKey(), entry.getValue());
    }
    JWTClaimsSet claimsSet = builder.build();
    return new PlainJWT(claimsSet);
  }

  private Map<String, Object> createAdditionalClaims(WithJwtAuthenticationToken tokenValues) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : createPathMap(tokenValues).entrySet()) {
      addClaim(entry.getKey(), entry.getValue(), map);
    }
    return map;
  }

  private void addClaim(String path, Object value, Map<String, Object> map) {
    int index = path.indexOf('.');
    if (index < 0) {
      map.put(path, value);
    } else {
      String key = path.substring(0, index);
      String nextPath = path.substring(index + 1);
      Object child = map.get(key);
      if (child instanceof Map) {
        //noinspection unchecked
        Map<String, Object> childMap = (Map<String, Object>) child;
        addClaim(nextPath, value, childMap);
      } else {
        Map<String, Object> childMap = new LinkedHashMap<>();
        map.put(key, childMap);
        addClaim(nextPath, value, childMap);
      }
    }
  }

  private Map<String, Object> createPathMap(WithJwtAuthenticationToken tokenValues) {
    Map<String, Object> map = new TreeMap<>();
    if (StringUtils.hasText(trimPath(tokenValues.rolesPath()))) {
      map.put(
          tokenValues.rolesPath(),
          Arrays.asList(tokenValues.roles()));
    }
    if (StringUtils.hasText(trimPath(tokenValues.scopePath()))) {
      map.put(
          tokenValues.scopePath(),
          Arrays.asList(tokenValues.scope()));
    }
    if (StringUtils.hasText(trimPath(tokenValues.namePath()))) {
      map.put(tokenValues.namePath(), tokenValues.name());
    }
    if (StringUtils.hasText(trimPath(tokenValues.preferredUsernamePath()))) {
      map.put(
          tokenValues.preferredUsernamePath(),
          tokenValues.preferredUsername());
    }
    if (StringUtils.hasText(trimPath(tokenValues.givenNamePath()))) {
      map.put(
          tokenValues.givenNamePath(),
          tokenValues.givenName());
    }
    if (StringUtils.hasText(trimPath(tokenValues.familyNamePath()))) {
      map.put(
          tokenValues.familyNamePath(),
          tokenValues.familyName());
    }
    if (StringUtils.hasText(trimPath(tokenValues.emailPath()))) {
      map.put(tokenValues.emailPath(), tokenValues.email());
    }
    return map;
  }

  private String trimPath(String path) {
    if (path == null) {
      return null;
    }
    String tmp = path
        .replace("..", ".")
        .trim();
    while (tmp.startsWith(".")) {
      tmp = tmp.substring(1).trim();
    }
    while (tmp.endsWith(".")) {
      tmp = tmp.substring(0, tmp.length() - 1).trim();
    }
    return tmp;
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
