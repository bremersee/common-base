package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ldaptive.pool.SearchValidator;

/**
 * The ldaptive properties test.
 */
class LdaptivePropertiesTest {

  /**
   * Is enabled.
   */
  @Test
  void isEnabled() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setEnabled(true);
    assertTrue(expected.isEnabled());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setEnabled(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));

    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
  }

  /**
   * Is use unbound id provider.
   */
  @Test
  void isUseUnboundIdProvider() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setUseUnboundIdProvider(true);
    assertTrue(expected.isUseUnboundIdProvider());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setUseUnboundIdProvider(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets ldap url.
   */
  @Test
  void getLdapUrl() {
    String value = UUID.randomUUID().toString();
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setLdapUrl(value);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setLdapUrl(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets connect timeout.
   */
  @Test
  void getConnectTimeout() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setConnectTimeout(123456789L);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setConnectTimeout(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Gets response timeout.
   */
  @Test
  void getResponseTimeout() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setResponseTimeout(123456789L);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setResponseTimeout(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Is use ssl.
   */
  @Test
  void isUseSsl() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setUseSsl(true);
    assertTrue(expected.isUseSsl());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setUseSsl(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Is use start tls.
   */
  @Test
  void isUseStartTls() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setUseStartTls(true);
    assertTrue(expected.isUseStartTls());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setUseStartTls(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets trust certificates.
   */
  @Test
  void getTrustCertificates() {
    String value = UUID.randomUUID().toString();
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setTrustCertificates(value);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setTrustCertificates(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets authentication certificate.
   */
  @Test
  void getAuthenticationCertificate() {
    String value = UUID.randomUUID().toString();
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setAuthenticationCertificate(value);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setAuthenticationCertificate(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets authentication key.
   */
  @Test
  void getAuthenticationKey() {
    String value = UUID.randomUUID().toString();
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setAuthenticationKey(value);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setAuthenticationKey(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets bind dn.
   */
  @Test
  void getBindDn() {
    String value = UUID.randomUUID().toString();
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setBindDn(value);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setBindDn(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Gets bind credential.
   */
  @Test
  void getBindCredential() {
    String value = UUID.randomUUID().toString();
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setBindCredentials(value);
    assertEquals(value, expected.getBindCredentials());
  }

  /**
   * Is pooled.
   */
  @Test
  void isPooled() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setPooled(true);
    assertTrue(expected.isPooled());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setPooled(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets min pool size.
   */
  @Test
  void getMinPoolSize() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setMinPoolSize(1234567);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setMinPoolSize(1234567);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("1234567"));
  }

  /**
   * Gets max pool size.
   */
  @Test
  void getMaxPoolSize() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setMaxPoolSize(1234567);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setMaxPoolSize(1234567);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("1234567"));
  }

  /**
   * Is validate on check in.
   */
  @Test
  void isValidateOnCheckIn() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setValidateOnCheckIn(true);
    assertTrue(expected.isValidateOnCheckIn());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setValidateOnCheckIn(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Is validate on check out.
   */
  @Test
  void isValidateOnCheckOut() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setValidateOnCheckOut(true);
    assertTrue(expected.isValidateOnCheckOut());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setValidateOnCheckOut(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Is validate periodically.
   */
  @Test
  void isValidatePeriodically() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setValidatePeriodically(true);
    assertTrue(expected.isValidatePeriodically());

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setValidatePeriodically(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Gets validate period.
   */
  @Test
  void getValidatePeriod() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setValidatePeriod(123456789L);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setValidatePeriod(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Gets prune period.
   */
  @Test
  void getPrunePeriod() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setPrunePeriod(123456789L);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setPrunePeriod(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Gets idle time.
   */
  @Test
  void getIdleTime() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setIdleTime(123456789L);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setIdleTime(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Gets block wait time.
   */
  @Test
  void getBlockWaitTime() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setBlockWaitTime(123456789L);

    LdaptiveProperties actual = new LdaptiveProperties();
    actual.setBlockWaitTime(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Gets search validator.
   */
  @Test
  void getSearchValidator() {
    LdaptiveProperties expected = new LdaptiveProperties();
    expected.setSearchValidator(new SearchValidator());
    assertNotNull(expected.getSearchValidator());
  }
}