package org.bremersee.test.security.authentication;

import static org.junit.Assert.assertTrue;

import org.bremersee.security.authentication.KeycloakJwtConverter;
import org.junit.Test;

/**
 * The keycloak jwt converter factory test.
 *
 * @author Christian Bremer
 */
public class KeycloakJwtConverterFactoryTest {

  /**
   * Create jwt converter.
   */
  @Test
  public void createJwtConverter() {
    JwtConverterFactory factory = new KeycloakJwtConverterFactory();
    assertTrue(factory.createJwtConverter() instanceof KeycloakJwtConverter);
  }
}