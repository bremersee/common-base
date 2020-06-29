package org.bremersee.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The message source properties test.
 */
class MessageSourcePropertiesTest {

  /**
   * Is always use message format.
   */
  @Test
  void isAlwaysUseMessageFormat() {
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setAlwaysUseMessageFormat(true);
    assertTrue(expected.isAlwaysUseMessageFormat());

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setAlwaysUseMessageFormat(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));

    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
  }

  /**
   * Is use code as default message.
   */
  @Test
  void isUseCodeAsDefaultMessage() {
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setUseCodeAsDefaultMessage(true);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setUseCodeAsDefaultMessage(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets base names.
   */
  @Test
  void getBaseNames() {
    String value = UUID.randomUUID().toString();
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setBaseNames(Arrays.asList(value, "value"));

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setBaseNames(Arrays.asList(value, "value"));

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets cache seconds.
   */
  @Test
  void getCacheSeconds() {
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setCacheSeconds(1234567);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setCacheSeconds(1234567);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("1234567"));
  }

  /**
   * Gets default encoding.
   */
  @Test
  void getDefaultEncoding() {
    String value = UUID.randomUUID().toString();
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setDefaultEncoding(value);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setDefaultEncoding(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Is fallback to system locale.
   */
  @Test
  void isFallbackToSystemLocale() {
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setFallbackToSystemLocale(true);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setFallbackToSystemLocale(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets default locale.
   */
  @Test
  void getDefaultLocale() {
    String value = UUID.randomUUID().toString();
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setDefaultLocale(value);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setDefaultLocale(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Is use reloadable message source.
   */
  @Test
  void isUseReloadableMessageSource() {
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setUseReloadableMessageSource(true);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setUseReloadableMessageSource(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Is concurrent refresh.
   */
  @Test
  void isConcurrentRefresh() {
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setConcurrentRefresh(true);

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setConcurrentRefresh(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets file encodings.
   */
  @Test
  void getFileEncodings() {
    String value = UUID.randomUUID().toString();
    MessageSourceProperties expected = new MessageSourceProperties();
    expected.setFileEncodings(Collections.singletonMap("key", value));

    MessageSourceProperties actual = new MessageSourceProperties();
    actual.setFileEncodings(Collections.singletonMap("key", value));

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Default locale.
   */
  @Test
  void defaultLocale() {
    MessageSourceProperties properties = new MessageSourceProperties();
    properties.setDefaultLocale("en-GB");
    assertNotNull(properties.defaultLocale());
    assertEquals(new Locale("en", "GB"), properties.defaultLocale());
  }

  /**
   * Default time zone.
   */
  @Test
  void defaultTimeZone() {
    MessageSourceProperties properties = new MessageSourceProperties();
    properties.setDefaultTimeZone("America/Santiago");
    assertNotNull(properties.defaultLocale());
    assertEquals(TimeZone.getTimeZone("America/Santiago"), properties.defaultTimeZone());
  }

}