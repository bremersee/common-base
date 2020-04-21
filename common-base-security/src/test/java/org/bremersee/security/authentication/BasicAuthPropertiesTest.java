package org.bremersee.security.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.security.authentication.BasicAuthProperties.Builder;
import org.junit.jupiter.api.Test;

/**
 * The basic auth properties test.
 *
 * @author Christian Bremer
 */
class BasicAuthPropertiesTest {

  /**
   * Gets username.
   */
  @Test
  void getUsername() {
    Builder model = BasicAuthProperties.builder();
    model.username("value");
    assertEquals("value", model.build().getUsername());

    assertNotEquals(model, null);
    assertNotEquals(model, new Object());
    assertEquals(model, model);
    assertEquals(model, BasicAuthProperties.builder().username("value"));

    assertTrue(model.toString().contains("value"));
  }

  /**
   * Gets password.
   */
  @Test
  void getPassword() {
    Builder model = BasicAuthProperties.builder();
    model.password("value");
    assertEquals("value", model.build().getPassword());
    assertFalse(model.toString().contains("value"));
  }
}