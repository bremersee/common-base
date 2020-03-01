/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.data.ldaptive;

import static org.springframework.util.Assert.notNull;

import java.time.Duration;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.Credential;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.util.StringUtils;

/**
 * The connection config factory.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
public interface LdaptiveConnectionConfigFactory {

  /**
   * Create connection config.
   *
   * @param properties the properties
   * @return the connection config
   */
  default ConnectionConfig createConnectionConfig(LdaptiveProperties properties) {
    notNull(properties, "Ldaptive properties must not be null.");
    return createConnectionConfig(
        properties,
        properties.getBindDn(),
        properties.getBindCredential());
  }

  /**
   * Create connection config.
   *
   * @param properties the properties
   * @param bindDn the bind dn
   * @param bindCredential the bind credential
   * @return the connection config
   */
  ConnectionConfig createConnectionConfig(
      LdaptiveProperties properties,
      String bindDn,
      String bindCredential);

  /**
   * Get default factory.
   *
   * @return the default connection config factory
   */
  static LdaptiveConnectionConfigFactory defaultFactory() {
    return new Default();
  }

  /**
   * The default connection config implementation.
   */
  class Default implements LdaptiveConnectionConfigFactory {

    @Override
    public ConnectionConfig createConnectionConfig(
        final LdaptiveProperties properties,
        final String bindDn,
        final String bindCredential) {

      notNull(properties, "Ldaptive properties must not be null.");
      final String username = bindDn != null ? bindDn : properties.getBindDn();
      final String password = bindCredential != null
          ? bindCredential
          : properties.getBindCredential();
      final ConnectionConfig cc = new ConnectionConfig();
      cc.setLdapUrl(properties.getLdapUrl());

      if (properties.getConnectTimeout() > 0L) {
        cc.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeout()));
      }
      if (properties.getResponseTimeout() > 0L) {
        cc.setResponseTimeout(Duration.ofMillis(properties.getResponseTimeout()));
      }

      cc.setUseSSL(properties.isUseSsl());
      cc.setUseStartTLS(properties.isUseStartTls());

      if ((properties.isUseSsl() || properties.isUseStartTls()) && hasSslConfig(properties)) {
        cc.setSslConfig(sslConfig(properties));
      }

      // binds all operations to a dn
      if (StringUtils.hasText(properties.getBindDn())) {
        cc.setConnectionInitializer(connectionInitializer(username, password));
      }

      return cc;
    }

    private boolean hasSslConfig(final LdaptiveProperties properties) {
      return StringUtils.hasText(properties.getTrustCertificates())
          || StringUtils.hasText(properties.getAuthenticationCertificate())
          || StringUtils.hasText(properties.getAuthenticationKey());
    }

    private SslConfig sslConfig(final LdaptiveProperties properties) {
      final SslConfig sc = new SslConfig();
      sc.setCredentialConfig(sslCredentialConfig(properties));
      return sc;
    }

    private CredentialConfig sslCredentialConfig(final LdaptiveProperties properties) {
      final X509CredentialConfig x509 = new X509CredentialConfig();
      if (StringUtils.hasText(properties.getAuthenticationCertificate())) {
        x509.setAuthenticationCertificate(properties.getAuthenticationCertificate());
      }
      if (StringUtils.hasText(properties.getAuthenticationKey())) {
        x509.setAuthenticationKey(properties.getAuthenticationKey());
      }
      if (StringUtils.hasText(properties.getTrustCertificates())) {
        x509.setTrustCertificates(properties.getTrustCertificates());
      }
      return x509;
    }

    private ConnectionInitializer connectionInitializer(String bindDn, String bindCredential) {
      // sasl is not supported at the moment
      final BindConnectionInitializer bci = new BindConnectionInitializer();
      bci.setBindDn(bindDn);
      bci.setBindCredential(new Credential(bindCredential));
      return bci;
    }

  }
}
