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

package org.bremersee.security.access;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.security.core.AuthorityConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The access control properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.security.acl")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AccessControlProperties {

  private Set<String> adminRoles = new LinkedHashSet<>();

  private boolean switchAdminAccess = true;

  private boolean returnNull = false;

  private Set<String> defaultPermissions = new LinkedHashSet<>();

  /**
   * Instantiates new access control properties.
   */
  public AccessControlProperties() {
    adminRoles.add(AuthorityConstants.ADMIN_ROLE_NAME);
    defaultPermissions.addAll(Arrays.asList(PermissionConstants.ALL));
  }
}
