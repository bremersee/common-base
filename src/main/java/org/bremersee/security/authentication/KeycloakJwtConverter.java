/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.security.authentication;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minidev.json.JSONValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public class KeycloakJwtConverter implements Converter<Jwt, JwtAuthenticationToken> {

  private static final String JSON_ROLES_PATH = "$.realm_access.roles";

  private static final com.jayway.jsonpath.Configuration jsonPathConf
      = com.jayway.jsonpath.Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

  @SuppressWarnings("WeakerAccess")
  public KeycloakJwtConverter() {
  }

  @Override
  public JwtAuthenticationToken convert(final Jwt jwt) {
    final String jsonStr = JSONValue.toJSONString(jwt.getClaims());
    final DocumentContext ctx = JsonPath.parse(jsonStr, jsonPathConf);
    final List<?> roles = ctx.read(JSON_ROLES_PATH, List.class);
    final Set<GrantedAuthority> authorities;
    if (roles == null || roles.isEmpty()) {
      authorities = Collections.emptySet();
    } else {
      authorities = roles
          .stream()
          .map(this::buildGrantedAuthority)
          .collect(Collectors.toSet());
    }
    return new KeycloakJwtAuthenticationToken(jwt, authorities);
  }

  private GrantedAuthority buildGrantedAuthority(final Object name) {
    final SimpleGrantedAuthority authority;
    if (name instanceof String && StringUtils.hasText((String) name)) {
      authority = new SimpleGrantedAuthority(((String) name).startsWith("ROLE_")
          ? (String) name : "ROLE_" + name);
    } else {
      authority = new SimpleGrantedAuthority("ROLE_NULL");
    }
    return authority;
  }
}
