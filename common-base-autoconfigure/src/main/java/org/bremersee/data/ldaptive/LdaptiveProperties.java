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

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.data.ldaptive.transcoder.UserAccountControlValueTranscoder;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * The ldap properties.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"WeakerAccess"})
@ConfigurationProperties(prefix = "bremersee.ldaptive")
@Validated
@Getter
@Setter
@ToString(exclude = {"bindCredentials"})
@EqualsAndHashCode(exclude = {"bindCredentials"})
@NoArgsConstructor
public class LdaptiveProperties {

  /**
   * Specifies whether ldap connection should be configured or not.
   */
  private boolean enabled = true;

  /**
   * Specifies whether an ldap user details service should be configured or not.
   */
  private boolean authenticationEnabled = false;

  /**
   * URL to the LDAP(s).
   */
  private String ldapUrl = "ldap://localhost:12389";

  /**
   * Duration of time that connects will block.
   */
  @NotNull
  private Duration connectTimeout = Duration.ofMinutes(1);

  /**
   * Duration of time to wait for responses.
   */
  @NotNull
  private Duration responseTimeout = Duration.ofMinutes(1);

  /**
   * Duration of time that operations will block on reconnects, should generally be longer than connect timeout.
   */
  @NotNull
  private Duration reconnectTimeout = Duration.ofMinutes(2);

  /**
   * Whether to automatically reconnect to the server when a connection is lost. Default is true.
   */
  private boolean autoReconnect = true;

  @Min(0)
  private int reconnectAttempts = 5;

  @NotNull
  private Duration reconnectBackoffDelay = Duration.ofSeconds(2);

  private double reconnectBackoffMultiplier = 1.;

  /**
   * Whether pending operations should be replayed after a reconnect. Default is true.
   */
  private boolean autoReplay = true;

  /**
   * Connect to LDAP using startTLS.
   */
  private boolean useStartTls;

  /**
   * Name of the trust certificates to use for the SSL connection.
   */
  private String trustCertificates;

  /**
   * Name of the authentication certificate to use for the SSL connection.
   */
  private String authenticationCertificate;

  /**
   * Name of the key to use for the SSL connection.
   */
  private String authenticationKey;

  /**
   * DN to bind as before performing operations.
   */
  private String bindDn;

  /**
   * Credential for the bind DN.
   */
  private String bindCredentials;

  /**
   * Specifies whether the connection should be pooled or not. Default is {@code false}.
   */
  private boolean pooled = false;

  /**
   * Duration to wait for an available connection.
   */
  @NotNull
  private Duration blockWaitTime = Duration.ofMinutes(1);

  /**
   * Minimum pool size.
   */
  private int minPoolSize = 3;

  /**
   * Maximum pool size.
   */
  private int maxPoolSize = 10;

  /**
   * Whether to connect to the ldap on connection creation.
   */
  private boolean connectOnCreate = true;

  /**
   * Whether initialize should throw if pooling configuration requirements are not met.
   */
  private boolean failFastInitialize = true;

  /**
   * Whether the ldap object should be validated when returned to the pool.
   */
  private boolean validateOnCheckIn = false;

  /**
   * Whether the ldap object should be validated when given from the pool.
   */
  private boolean validateOnCheckOut = false;

  /**
   * Whether the pool should be validated periodically.
   */
  private boolean validatePeriodically = false;

  /**
   * Validation period.
   */
  @NotNull
  private Duration validatePeriod = Duration.ofMinutes(30);

  /**
   * Maximum length of time a connection validation should block.
   */
  @NotNull
  private Duration validateTimeout = Duration.ofSeconds(5);

  @NotNull
  private SearchValidatorProperties searchValidator = new SearchValidatorProperties();

  /**
   * Prune period.
   */
  @NotNull
  private Duration prunePeriod = Duration.ofMinutes(5);

  /**
   * Idle time.
   */
  @NotNull
  private Duration idleTime = Duration.ofMinutes(10);

  @NotNull
  private UserDetailsProperties userDetails = new UserDetailsProperties();

  /**
   * Create search connection validator search connection validator.
   *
   * @return the search connection validator
   */
  @NotNull
  public SearchConnectionValidator createSearchConnectionValidator() {
    return new SearchConnectionValidator(
        validatePeriod,
        validateTimeout,
        searchValidator.getSearchRequest().createSearchRequest());
  }

  /**
   * The search validator properties.
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class SearchValidatorProperties {

    @NotNull
    private SearchRequestProperties searchRequest = new SearchRequestProperties();

    /**
     * The search request properties.
     */
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class SearchRequestProperties {

      private String baseDn;

      @NotNull
      private SearchFilterProperties searchFilter = new SearchFilterProperties();

      private Integer sizeLimit;

      private SearchScope searchScope; // = SearchScope.ONELEVEL;

      @NotNull
      private List<String> returnAttributes = new ArrayList<>();

      /**
       * Gets the return attributes as array.
       *
       * @return the return attributes as array
       */
      @NotNull
      public String[] returnAttributesAsArray() {
        if (returnAttributes.isEmpty()) {
          return ReturnAttributes.NONE.value();
        }
        return returnAttributes.toArray(new String[0]);
      }

      /**
       * Create search request.
       *
       * @return the search request
       */
      @NotNull
      public SearchRequest createSearchRequest() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setBaseDn(StringUtils.hasText(getBaseDn()) ? getBaseDn() : "");
        if (StringUtils.hasText(getSearchFilter().getFilter())) {
          searchRequest.setFilter(getSearchFilter().getFilter());
        }
        searchRequest.setReturnAttributes(returnAttributesAsArray());
        if (getSearchScope() != null) {
          searchRequest.setSearchScope(getSearchScope());
        }
        if (getSizeLimit() != null) {
          searchRequest.setSizeLimit(getSizeLimit());
        }
        return searchRequest;
      }

      /**
       * The search filter properties.
       */
      @Getter
      @Setter
      @ToString
      @EqualsAndHashCode
      @NoArgsConstructor
      public static class SearchFilterProperties {

        private String filter;

      }
    }
  }

  /**
   * The user details properties.
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class UserDetailsProperties {

    private String userBaseDn;

    private String userFindOneFilter = "(&(objectClass=group)(sAMAccountName={0}))";

    private SearchScope userFindOneSearchScope = SearchScope.ONELEVEL;

    private String userAccountControlAttributeName = UserAccountControlValueTranscoder.ATTRIBUTE_NAME;

    private String authorityAttributeName = "memberOf";

    private boolean authorityDn = true;

    private List<String> authorities = new LinkedList<>();

    private Map<String, String> authorityMap = new LinkedHashMap<>();

    private String authorityPrefix = "ROLE_";

    private String userPasswordAttributeName = "userPassword";

    private String userPasswordLabel = "SHA";

    private String userPasswordAlgorithm = "SHA";

  }
}
