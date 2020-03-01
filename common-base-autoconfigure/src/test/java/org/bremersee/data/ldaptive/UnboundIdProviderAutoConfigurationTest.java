package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.unboundid.UnboundIDProvider;

/**
 * The unbound id provider auto configuration test.
 */
class UnboundIdProviderAutoConfigurationTest {

  /**
   * Unbound id provider.
   */
  @Test
  void unboundIdProvider() {
    Provider<?> provider = new UnboundIdProviderAutoConfiguration().unboundIdProvider();
    assertNotNull(provider);
    assertTrue(provider instanceof UnboundIDProvider);
  }
}