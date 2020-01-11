package org.bremersee.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * The message source auto configuration test.
 */
class MessageSourceAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    MessageSourceAutoConfiguration configuration = new MessageSourceAutoConfiguration(
        new MessageSourceProperties());
    configuration.init();
  }

  /**
   * Message source.
   */
  @Test
  void messageSource() {
    MessageSourceProperties properties = new MessageSourceProperties();
    properties.setUseReloadableMessageSource(false);
    MessageSourceAutoConfiguration configuration = new MessageSourceAutoConfiguration(
        properties);
    MessageSource messageSource = configuration.messageSource();
    assertNotNull(messageSource);
    assertTrue(messageSource instanceof ResourceBundleMessageSource);

    properties.setUseReloadableMessageSource(true);
    configuration = new MessageSourceAutoConfiguration(properties);
    messageSource = configuration.messageSource();
    assertNotNull(messageSource);
    assertTrue(messageSource instanceof ReloadableResourceBundleMessageSource);
  }
}