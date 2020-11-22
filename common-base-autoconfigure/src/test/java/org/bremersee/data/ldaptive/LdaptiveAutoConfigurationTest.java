package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
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

    return new LdaptiveAutoConfiguration(
        properties,
        connectionConfigFactory);
  }
}