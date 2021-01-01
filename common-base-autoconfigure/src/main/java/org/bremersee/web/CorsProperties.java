/*
 * Copyright 2019-2020 the original author or authors.
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

package org.bremersee.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The cors properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.cors")
@Setter
@ToString
@EqualsAndHashCode
@Validated
public class CorsProperties {

  @Getter
  private boolean enable = true;

  @Getter
  private boolean allowAll = true;

  @NotNull
  private List<CorsConfiguration> configs = new ArrayList<>();

  /**
   * Allow all configuration.
   *
   * @return the allow all configuration
   */
  @NotEmpty
  public static List<CorsConfiguration> allowAllConfiguration() {
    return Collections
        .singletonList(CorsConfiguration.allowAllConfiguration());
  }

  /**
   * Gets configs.
   *
   * @return the configs
   */
  @NotNull
  public List<CorsConfiguration> getConfigs() {
    if (configs == null) {
      configs = new ArrayList<>();
    }
    if (enable && allowAll && configs.isEmpty()) {
      return allowAllConfiguration();
    }
    return configs.stream()
        .filter(config -> StringUtils.hasText(config.getPathPattern()))
        .collect(Collectors.toList());
  }

  /**
   * The cors configuration.
   */
  @Setter
  @ToString
  @EqualsAndHashCode
  @Validated
  public static class CorsConfiguration {

    @Getter
    private String pathPattern;

    private List<String> allowedOrigins = new ArrayList<>();

    private List<String> allowedMethods = new ArrayList<>();

    private List<String> allowedHeaders = new ArrayList<>();

    @Getter
    @NotNull
    private List<String> exposedHeaders = new ArrayList<>();

    @Getter
    private boolean allowCredentials;

    @Getter
    private long maxAge = 1800L;

    /**
     * Allow all configuration.
     *
     * @return the allow all configuration
     */
    static CorsConfiguration allowAllConfiguration() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.pathPattern = "/**";
      configuration.allowedOrigins = Collections.singletonList("*");
      configuration.allowedMethods = Collections.singletonList("*");
      configuration.allowedHeaders = Collections.singletonList("*");
      configuration.allowCredentials = false;
      return configuration;
    }

    /**
     * Gets allowed origins.
     *
     * @return the allowed origins
     */
    @NotEmpty
    public List<String> getAllowedOrigins() {
      if (allowedOrigins == null) {
        allowedOrigins = new ArrayList<>();
      }
      if (allowedOrigins.isEmpty()) {
        allowedOrigins.add("*");
      }
      return allowedOrigins;
    }

    /**
     * Gets allowed methods.
     *
     * @return the allowed methods
     */
    @NotEmpty
    public List<String> getAllowedMethods() {
      if (allowedMethods == null) {
        allowedMethods = new ArrayList<>();
      }
      if (allowedMethods.isEmpty()) {
        allowedMethods.add("*");
      }
      return allowedMethods;
    }

    /**
     * Gets allowed headers.
     *
     * @return the allowed headers
     */
    @NotEmpty
    public List<String> getAllowedHeaders() {
      if (allowedHeaders == null) {
        allowedHeaders = new ArrayList<>();
      }
      if (allowedHeaders.isEmpty()) {
        allowedHeaders.add("*");
      }
      return allowedHeaders;
    }
  }
}
