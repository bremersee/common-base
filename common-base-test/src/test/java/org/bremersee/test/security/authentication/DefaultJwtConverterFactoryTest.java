package org.bremersee.test.security.authentication;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * The default jwt converter factory test.
 *
 * @author Christian Bremer
 */
public class DefaultJwtConverterFactoryTest {

  /**
   * Create jwt converter.
   */
  @Test
  public void createJwtConverter() {
    JwtConverterFactory factory = new DefaultJwtConverterFactory();
    assertTrue(factory.createJwtConverter() instanceof JwtAuthenticationConverter);
  }
}