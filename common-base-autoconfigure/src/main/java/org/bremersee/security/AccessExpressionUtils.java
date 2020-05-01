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

package org.bremersee.security;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.AccessMode;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.EurekaAccessProperties;
import org.bremersee.security.SecurityProperties.AuthenticationProperties.PathMatcherProperties;
import org.springframework.util.StringUtils;

/**
 * The access expression utilities.
 *
 * @author Christian Bremer
 */
public abstract class AccessExpressionUtils {

  /**
   * The deny all spring expression.
   */
  static final String DENY_ALL = "denyAll";

  /**
   * The permit all spring expression.
   */
  static final String PERMIT_ALL = "permitAll";

  /**
   * The is authenticated spring expression.
   */
  static final String IS_AUTHENTICATED = "isAuthenticated()";

  private static final String HAS_AUTHORITY_TEMPLATE = "hasAuthority('%s')";

  private static final String HAS_ANY_AUTHORITY_TEMPLATE = "hasAnyAuthority(%s)";

  private static final String HAS_IP_ADDRESS_TEMPLATE = "hasIpAddress('%s')";

  private AccessExpressionUtils() {
  }

  /**
   * Builds the {@code hasAuthority} expression.
   *
   * @param role the role
   * @param ensurePrefixFunction the ensure prefix function
   * @return the {@code hasAuthority} expression
   */
  public static String hasAuthorityExpr(
      String role,
      Function<String, String> ensurePrefixFunction) {

    return String.format(
        HAS_AUTHORITY_TEMPLATE,
        Optional.ofNullable(ensurePrefixFunction)
            .map(f -> f.apply(role))
            .orElse(role));
  }

  /**
   * Builds the {@code hasAnyAuthority} expression.
   *
   * @param roles the roles
   * @param ensurePrefixFunction the ensure prefix function
   * @return the {@code hasAnyAuthority} expression
   */
  public static String hasAnyAuthorityExpr(
      Collection<String> roles,
      Function<String, String> ensurePrefixFunction) {

    return Optional.ofNullable(roles)
        .map(list -> hasAnyAuthorityExprNullSave(
            list.stream().filter(StringUtils::hasText).collect(Collectors.toList()),
            ensurePrefixFunction))
        .orElse("");
  }

  private static String hasAnyAuthorityExprNullSave(
      Collection<String> roles,
      Function<String, String> ensurePrefixFunction) {

    return Optional.of(roles.size())
        .filter(size -> size > 1)
        .map(size -> roles.stream()
            .filter(StringUtils::hasText)
            .map(role -> Optional.ofNullable(ensurePrefixFunction)
                .map(f -> f.apply(role))
                .orElse(role))
            .map(role -> "'" + role + "'")
            .collect(Collectors.joining(",")))
        .map(value -> String.format(HAS_ANY_AUTHORITY_TEMPLATE, value))
        .orElseGet(() -> hasAuthorityExpr(roles.stream()
            .findFirst().orElse(""), ensurePrefixFunction));
  }

  /**
   * Builds the {@code hasIpAddress} expression.
   *
   * @param ip the ip
   * @return the {@code hasIpAddress} expression
   */
  public static String hasIpAddressExpr(String ip) {
    if (StringUtils.hasText(ip)) {
      return String.format(HAS_IP_ADDRESS_TEMPLATE, ip);
    }
    return "";
  }

  /**
   * Builds the {@code hasIpAddress} expression.
   *
   * @param ips the ips
   * @return the {@code hasIpAddress} expression
   */
  public static String hasIpAddressExpr(Collection<String> ips) {
    return Optional.ofNullable(ips)
        .map(list -> list.stream()
            .filter(StringUtils::hasText)
            .map(AccessExpressionUtils::hasIpAddressExpr)
            .collect(Collectors.joining(" or ")))
        .orElse("");
  }

  /**
   * Builds an access expression from the given roles and ip addresses.
   *
   * @param roles the roles
   * @param ensurePrefixFunction the ensure prefix function
   * @param ips the ips
   * @return the access expression
   */
  public static String hasAuthorityOrIpAddressExpr(
      Collection<String> roles,
      Function<String, String> ensurePrefixFunction,
      Collection<String> ips) {

    TreeSet<String> roleSet = roles instanceof TreeSet
        ? (TreeSet<String>) roles
        : roles == null ? new TreeSet<>() : new TreeSet<>(roles);
    StringBuilder sb = new StringBuilder();
    if (roleSet.size() > 1) {
      sb.append(hasAnyAuthorityExpr(roleSet, ensurePrefixFunction));
    } else if (roleSet.size() == 1) {
      sb.append(hasAuthorityExpr(roleSet.first(), ensurePrefixFunction));
    }
    Set<String> ipSet = ips instanceof Set
        ? (Set<String>) ips
        : ips == null ? Collections.emptySet() : new LinkedHashSet<>(ips);
    if (!ipSet.isEmpty()) {
      if (sb.length() > 0) {
        sb.append(" or ");
      }
      sb.append(hasIpAddressExpr(ipSet));
    }
    return sb.toString();
  }

  /**
   * Build access expression of the given path matcher properties.
   *
   * @param properties the properties
   * @param ensureRolePrefixFunction the ensure role prefix function
   * @return the access expression
   */
  static String buildAccessExpression(
      PathMatcherProperties properties,
      Function<String, String> ensureRolePrefixFunction) {

    if (AccessMode.AUTHENTICATED == properties.getAccessMode()) {
      StringBuilder sb = new StringBuilder();
      sb.append(hasAuthorityOrIpAddressExpr(
          properties.getRoles(),
          ensureRolePrefixFunction,
          properties.getIpAddresses()));
      if (properties.getRoles() == null || properties.getRoles().isEmpty()) {
        if (sb.length() > 0) {
          sb.append(" or ");
        }
        sb.append(IS_AUTHENTICATED);
      }
      return sb.toString();
    }
    return properties.getAccessMode().getExpressionValue();
  }

  /**
   * Build access expression of the given eureka access properties.
   *
   * @param properties the properties
   * @param ensureRolePrefixFunction the ensure role prefix function
   * @return the access expression
   */
  static String buildAccessExpression(
      EurekaAccessProperties properties,
      Function<String, String> ensureRolePrefixFunction) {

    if (StringUtils.hasText(properties.getRole())) {
      return hasAuthorityOrIpAddressExpr(
          Collections.singleton(properties.getRole()),
          ensureRolePrefixFunction,
          properties.getIpAddresses());
    }
    String ipsExpr = hasIpAddressExpr(properties.getIpAddresses());
    return StringUtils.hasText(ipsExpr)
        ? IS_AUTHENTICATED + " or " + ipsExpr
        : IS_AUTHENTICATED;
  }

}
