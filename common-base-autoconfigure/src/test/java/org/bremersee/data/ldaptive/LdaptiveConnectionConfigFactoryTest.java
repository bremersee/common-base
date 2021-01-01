package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.ldaptive.ConnectionConfig;

/**
 * The ldaptive connection config factory test.
 */
class LdaptiveConnectionConfigFactoryTest {

  /**
   * Create connection config.
   */
  @Test
  void createConnectionConfig() {
    LdaptiveProperties properties = new LdaptiveProperties();
    properties.setPooled(false);
    properties.setConnectTimeout(Duration.ofMinutes(1L));
    properties.setResponseTimeout(Duration.ofMinutes(2L));
    properties.setUseStartTls(true);
    properties.setTrustCertificates("trustcert");
    properties.setAuthenticationCertificate("authcert");
    properties.setAuthenticationKey("authkey");
    properties.setBindDn("ou=admin");
    properties.setBindCredentials("changeit");
    ConnectionConfig config = LdaptiveConnectionConfigFactory
        .defaultFactory()
        .createConnectionConfig(properties);
    assertNotNull(config);
    config = LdaptiveConnectionConfigFactory
        .defaultFactory()
        .createConnectionConfig(properties, "ou=admin", "changeit");
    assertNotNull(config);
  }

}