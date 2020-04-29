package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    properties.setConnectTimeout(1);
    properties.setResponseTimeout(2);
    properties.setUseStartTls(true);
    properties.setUseSsl(true);
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