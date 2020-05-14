package org.bremersee.data.ldaptive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.ConnectionPool;

/**
 * The ldaptive connection pool factory test.
 */
class LdaptiveConnectionPoolFactoryTest {

  /**
   * Create connection pool.
   */
  @Test
  void createConnectionPool() {
    LdaptiveProperties properties = new LdaptiveProperties();
    properties.setPooled(true);
    properties.setConnectTimeout(1);
    properties.setResponseTimeout(2);
    properties.setUseStartTls(true);
    properties.setUseSsl(true);
    properties.setTrustCertificates("trustcert");
    properties.setAuthenticationCertificate("authcert");
    properties.setAuthenticationKey("authkey");
    properties.setBindDn("ou=admin");
    properties.setBindCredentials("changeit");

    properties.setPrunePeriod(1L);
    properties.setValidatePeriod(2L);
    properties.setValidatePeriodically(true);
    properties.setMinPoolSize(3);
    properties.setMaxPoolSize(4);

    ConnectionPool pool = LdaptiveConnectionPoolFactory
        .defaultFactory()
        .createConnectionPool(properties, mock(DefaultConnectionFactory.class));
    assertNotNull(pool);
  }
}