package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.ldaptive.provider.Provider;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The ldaptive auto configuration test.
 */
class LdaptiveAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    LdaptiveProperties properties = new LdaptiveProperties();
    properties.setPooled(false);
    LdaptiveAutoConfiguration configuration = buildConfiguration(properties);
    configuration.init();
  }

  /**
   * Ldaptive template.
   */
  @Test
  void ldaptiveTemplate() {
    LdaptiveProperties properties = new LdaptiveProperties();
    properties.setPooled(false);

    LdaptiveAutoConfiguration configuration = buildConfiguration(properties);
    assertNotNull(configuration.ldaptiveTemplate(configuration.connectionFactory()));
  }

  /**
   * Connection factory.
   */
  @Test
  void connectionFactory() {
    LdaptiveProperties properties = new LdaptiveProperties();
    properties.setPooled(false);

    LdaptiveAutoConfiguration configuration = buildConfiguration(properties);
    assertNotNull(configuration.connectionFactory());
  }

  @SuppressWarnings("unchecked")
  private static LdaptiveAutoConfiguration buildConfiguration(LdaptiveProperties properties) {
    ObjectProvider<LdaptiveConnectionConfigFactory> connectionConfigFactory
        = mock(ObjectProvider.class);
    when(connectionConfigFactory.getIfAvailable(any()))
        .thenReturn(LdaptiveConnectionConfigFactory.defaultFactory());
    ObjectProvider<LdaptiveConnectionPoolFactory> connectionPoolFactory
        = mock(ObjectProvider.class);
    when(connectionPoolFactory.getIfAvailable(any()))
        .thenReturn(LdaptiveConnectionPoolFactory.defaultFactory());
    ObjectProvider<Provider<?>> ldaptiveProvider
        = mock(ObjectProvider.class);
    when(ldaptiveProvider.getIfAvailable())
        .thenReturn(null);

    return new LdaptiveAutoConfiguration(
        properties,
        connectionConfigFactory,
        connectionPoolFactory,
        ldaptiveProvider);
  }
}