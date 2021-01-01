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

import java.util.function.Predicate;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ClosedRetryMetadata;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.Credential;
import org.ldaptive.RetryMetadata;
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
        properties.getBindCredentials());
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
   * Get default connection config factory.
   *
   * @return the default connection config factory
   */
  static LdaptiveConnectionConfigFactory defaultFactory() {
    return new Default();
  }

  /**
   * The default connection config factory.
   */
  class Default implements LdaptiveConnectionConfigFactory {

    @Override
    public ConnectionConfig createConnectionConfig(
        final LdaptiveProperties properties,
        final String bindDn,
        final String bindCredentials) {

      notNull(properties, "Ldaptive properties must not be null.");
      return ConnectionConfig.builder()
          .autoReconnect(properties.isAutoReconnect())
          .autoReconnectCondition(autoReconnectCondition(properties))
          .autoReplay(properties.isAutoReplay())
          .connectionInitializers(connectionInitializers(properties, bindDn, bindCredentials))
          .connectTimeout(properties.getConnectTimeout())
          .reconnectTimeout(properties.getReconnectTimeout())
          .responseTimeout(properties.getResponseTimeout())
          .sslConfig(sslConfig(properties))
          .url(properties.getLdapUrl())
          .useStartTLS(properties.isUseStartTls())
          .build();
    }

    private Predicate<RetryMetadata> autoReconnectCondition(LdaptiveProperties properties) {
      return metadata -> {
        if (properties.getReconnectAttempts() > 0 && metadata instanceof ClosedRetryMetadata) {
          if (metadata.getAttempts() > properties.getReconnectAttempts()) {
            return false;
          }
          if (metadata.getAttempts() > 0) {
            try {
              long delay = Math.abs(properties.getReconnectBackoffDelay().toMillis());
              double multiplier = Math.abs(properties.getReconnectBackoffMultiplier() * metadata.getAttempts());
              int attempts = metadata.getAttempts();
              long millis = Math.round(delay * multiplier * attempts);
              Thread.sleep(millis);
            } catch (InterruptedException e) {
              // nothing to do
            }
          }
          return true;
        }
        return false;
      };
    }

    private ConnectionInitializer[] connectionInitializers(
        LdaptiveProperties properties,
        String bindDn,
        String bindCredentials) {

      String username;
      String password;
      if (StringUtils.hasText(bindDn)) {
        username = bindDn;
        password = bindCredentials;
      } else {
        username = properties.getBindDn();
        password = properties.getBindCredentials();
      }
      if (StringUtils.hasText(username)) {
        return new ConnectionInitializer[]{
            connectionInitializer(username, password)
        };
      }
      return new ConnectionInitializer[]{};
    }

    private ConnectionInitializer connectionInitializer(String bindDn, String bindCredential) {
      // sasl is not supported at the moment
      final BindConnectionInitializer bci = new BindConnectionInitializer();
      bci.setBindDn(bindDn);
      bci.setBindCredential(new Credential(bindCredential));
      return bci;
    }

    private SslConfig sslConfig(LdaptiveProperties properties) {
      if (hasSslConfig(properties)) {
        SslConfig sc = new SslConfig();
        sc.setCredentialConfig(sslCredentialConfig(properties));
        return sc;
      }
      return null;
    }

    private boolean hasSslConfig(LdaptiveProperties properties) {
      return StringUtils.hasText(properties.getTrustCertificates())
          || StringUtils.hasText(properties.getAuthenticationCertificate())
          || StringUtils.hasText(properties.getAuthenticationKey());
    }

    private CredentialConfig sslCredentialConfig(LdaptiveProperties properties) {
      X509CredentialConfig x509 = new X509CredentialConfig();
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

  }
}
