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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.bremersee.security.SecurityProperties.CorsProperties;
import org.bremersee.security.SecurityProperties.CorsProperties.CorsConfiguration;
import org.junit.jupiter.api.Test;

/**
 * The cors properties test.
 *
 * @author Christian Bremer
 */
class CorsPropertiesTest {

  /**
   * Tests allow all configuration.
   */
  @Test
  void allowAllConfiguration() {
    List<CorsConfiguration> configurations = CorsProperties.allowAllConfiguration();
    assertNotNull(configurations);
    assertTrue(configurations.contains(CorsConfiguration.allowAllConfiguration()));
  }

  /**
   * Tests configs.
   */
  @Test
  void configs() {
    CorsProperties properties = new CorsProperties();
    properties.setAllowAll(true);
    List<CorsConfiguration> configurations = properties.getConfigs();
    assertNotNull(configurations);
    assertTrue(configurations.contains(CorsConfiguration.allowAllConfiguration()));

    properties.setAllowAll(false);
    configurations = properties.getConfigs();
    assertNotNull(configurations);
    assertTrue(configurations.isEmpty());

    CorsConfiguration c0 = new CorsConfiguration();
    c0.setAllowCredentials(true);
    c0.setAllowedHeaders(Arrays.asList("x-foo", "x-bar"));
    c0.setAllowedMethods(Arrays.asList("GET", "POST"));
    c0.setAllowedOrigins(Arrays.asList("localhost", "example.org"));
    c0.setMaxAge(3600L);
    c0.setPathPattern("/bar/**");

    assertTrue(c0.getAllowedHeaders().contains("x-foo"));
    assertTrue(c0.getAllowedHeaders().contains("x-bar"));
    assertFalse(c0.getAllowedHeaders().contains("*"));

    assertTrue(c0.getAllowedMethods().contains("GET"));
    assertTrue(c0.getAllowedMethods().contains("POST"));
    assertFalse(c0.getAllowedMethods().contains("PUT"));
    assertFalse(c0.getAllowedMethods().contains("*"));

    assertTrue(c0.getAllowedOrigins().contains("localhost"));
    assertTrue(c0.getAllowedOrigins().contains("example.org"));
    assertFalse(c0.getAllowedOrigins().contains("*"));

    assertTrue(c0.isAllowCredentials());
    assertEquals(3600L, c0.getMaxAge());
    assertEquals("/bar/**", c0.getPathPattern());

    CorsConfiguration c1 = new CorsConfiguration();
    c1.setPathPattern("/foo/**");

    assertTrue(c1.getAllowedHeaders().contains("*"));
    assertTrue(c1.getAllowedMethods().contains("*"));
    assertTrue(c1.getAllowedOrigins().contains("*"));

    CorsConfiguration c2 = new CorsConfiguration();
    properties.setConfigs(Arrays.asList(c0, c1, c2));

    configurations = properties.getConfigs();
    assertNotNull(configurations);
    assertEquals(2, configurations.size());
    assertTrue(configurations.contains(c0));
    assertTrue(configurations.contains(c1));
    assertFalse(configurations.contains(c2));
  }

  /**
   * Tests allow all.
   */
  @Test
  void allowAll() {
    CorsProperties properties = new CorsProperties();
    properties.setAllowAll(true);
    assertTrue(properties.isAllowAll());
    properties.setAllowAll(false);
    assertFalse(properties.isAllowAll());
  }

  /**
   * Tests to string and equals.
   */
  @Test
  void toStringAndEquals() {
    CorsProperties cp0 = new CorsProperties();
    cp0.setConfigs(CorsProperties.allowAllConfiguration());
    assertEquals(cp0, cp0);
    CorsProperties cp1 = new CorsProperties();
    cp1.setConfigs(CorsProperties.allowAllConfiguration());
    assertEquals(cp0, cp1);
    assertNotNull(cp0.toString());
  }

}