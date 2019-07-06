package org.bremersee.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.bremersee.security.CorsProperties.CorsConfiguration;
import org.junit.Test;

/**
 * The cors properties test.
 *
 * @author Christian Bremer
 */
public class CorsPropertiesTest {

  /**
   * Tests allow all configuration.
   */
  @Test
  public void allowAllConfiguration() {
    List<CorsConfiguration> configurations = CorsProperties.allowAllConfiguration();
    assertNotNull(configurations);
    assertTrue(configurations.contains(CorsConfiguration.allowAllConfiguration()));
  }

  /**
   * Tests configs.
   */
  @Test
  public void configs() {
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
    assertTrue(c1.getAllowedMethods().contains("GET"));
    assertTrue(c1.getAllowedMethods().contains("POST"));
    assertTrue(c1.getAllowedMethods().contains("HEAD"));
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
  public void allowAll() {
    CorsProperties properties = new CorsProperties();
    properties.setAllowAll(true);
    assertTrue(properties.isAllowAll());
    properties.setAllowAll(false);
    assertFalse(properties.isAllowAll());
  }

}