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

package org.bremersee.security.authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * The json path jwt converter.
 *
 * @author Christian Bremer
 */
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public class JsonPathJwtConverter implements Converter<Jwt, JwtAuthenticationToken> {

  private String rolesJsonPath = "$.scope";

  private boolean rolesValueList = false;

  private String rolesValueSeparator = " ";

  private String rolePrefix = "SCOPE_";

  private String nameJsonPath = "$.sub";

  /**
   * Sets roles json path.
   *
   * @param rolesJsonPath the roles json path
   */
  public void setRolesJsonPath(String rolesJsonPath) {
    if (StringUtils.hasText(rolesJsonPath)) {
      this.rolesJsonPath = rolesJsonPath;
    }
  }

  /**
   * Sets roles value list.
   *
   * @param rolesValueList the roles value list
   */
  public void setRolesValueList(boolean rolesValueList) {
    this.rolesValueList = rolesValueList;
  }

  /**
   * Sets roles value separator.
   *
   * @param rolesValueSeparator the roles value separator
   */
  public void setRolesValueSeparator(String rolesValueSeparator) {
    if (rolesValueSeparator != null) {
      this.rolesValueSeparator = rolesValueSeparator;
    }
  }

  /**
   * Sets role prefix.
   *
   * @param rolePrefix the role prefix
   */
  public void setRolePrefix(String rolePrefix) {
    if (rolePrefix != null) {
      this.rolePrefix = rolePrefix;
    }
  }

  /**
   * Sets name json path.
   *
   * @param nameJsonPath the name json path
   */
  public void setNameJsonPath(String nameJsonPath) {
    if (StringUtils.hasText(nameJsonPath)) {
      this.nameJsonPath = nameJsonPath;
    }
  }

  @NonNull
  @Override
  public JwtAuthenticationToken convert(@NonNull final Jwt source) {
    final JsonPathJwtParser parser = new JsonPathJwtParser(source);
    final List<String> roleValues;
    if (rolesValueList) {
      final List<?> rawList = parser.read(rolesJsonPath, List.class);
      roleValues = rawList == null
          ? Collections.emptyList()
          : rawList.stream()
              .filter(Objects::nonNull)
              .map(Object::toString)
              .collect(Collectors.toList());
    } else {
      final String roleValue = parser.read(rolesJsonPath, String.class);
      if (StringUtils.hasText(roleValue)) {
        roleValues = Arrays.asList(roleValue.split(rolesValueSeparator));
      } else {
        roleValues = Collections.emptyList();
      }
    }
    final Set<GrantedAuthority> authorities = roleValues.stream()
        .filter(roleName -> roleName.trim().length() > 0)
        .map(roleName -> roleName.startsWith(rolePrefix) ? roleName : rolePrefix + roleName)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
    final String name = parser.read(nameJsonPath, String.class);
    return new JwtAuthenticationToken(
        source,
        authorities,
        StringUtils.hasText(name) ? name : source.getSubject());
  }

}
