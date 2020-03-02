package org.bremersee.security.authentication;

import static org.junit.jupiter.api.Assertions.*;

import org.bremersee.security.authentication.BasicAuthProperties.Builder;
import org.junit.jupiter.api.Test;

class BasicAuthPropertiesTest {

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

  @Test
  void getPassword() {
    Builder model = BasicAuthProperties.builder();
    model.password("value");
    assertEquals("value", model.build().getPassword());
    assertFalse(model.toString().contains("value"));
  }
}