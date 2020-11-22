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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.ldaptive.AddRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.PasswordModifyRequest;
import org.ldaptive.extended.PasswordModifyResponseParser;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The interface for ldap operations.
 *
 * @author Christian Bremer
 */
@Validated
public interface LdaptiveOperations {

  /**
   * Gets connection factory.
   *
   * @return the connection factory
   */
  @NotNull
  ConnectionFactory getConnectionFactory();

  /**
   * Executes add operation.
   *
   * @param addRequest the add request
   */
  void add(@NotNull AddRequest addRequest);

  /**
   * Executes bind operation.
   *
   * @param request the request
   * @return the boolean
   */
  boolean bind(@NotNull BindRequest request);

  /**
   * Executes compare operation.
   *
   * @param request the request
   * @return the boolean
   */
  boolean compare(@NotNull CompareRequest request);

  /**
   * Executes delete operation.
   *
   * @param request the request
   */
  void delete(@NotNull DeleteRequest request);

  /**
   * Executes an extended request.
   *
   * @param request the request
   * @return the extended response
   */
  @NotNull
  ExtendedResponse executeExtension(@NotNull ExtendedRequest request);

  /**
   * Generate user password.
   *
   * @param dn the dn
   * @return the generated user password
   */
  default String generateUserPassword(@NotNull String dn) {
    ExtendedResponse response = executeExtension(new PasswordModifyRequest(dn));
    return PasswordModifyResponseParser.parse(response);
  }

  /**
   * Executes modify operation.
   *
   * @param request the request
   */
  void modify(@NotNull ModifyRequest request);

  /**
   * Executes modify dn operation.
   *
   * @param request the request
   */
  void modifyDn(@NotNull ModifyDnRequest request);

  /**
   * Modifies user password.
   *
   * @param dn the dn
   * @param oldPass the old pass
   * @param newPass the new pass
   */
  default void modifyUserPassword(@NotNull String dn, @Nullable String oldPass, @Nullable String newPass) {
    executeExtension(new PasswordModifyRequest(dn, oldPass, newPass));
  }

  /**
   * Executes search operation.
   *
   * @param request the request
   * @return the search response
   */
  @NotNull
  SearchResponse search(@NotNull SearchRequest request);

  /**
   * Find one.
   *
   * @param request the request
   * @return the optional ldap entry
   */
  default Optional<LdapEntry> findOne(@NotNull SearchRequest request) {
    return Optional.ofNullable(search(request))
        .map(SearchResponse::getEntry);
  }

  /**
   * Find one.
   *
   * @param <T> the type parameter
   * @param request the request
   * @param entryMapper the entry mapper
   * @return the optional domain object
   */
  default <T> Optional<T> findOne(
      @NotNull SearchRequest request,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return Optional.ofNullable(search(request))
        .map(SearchResponse::getEntry)
        .map(entryMapper::map);
  }

  /**
   * Find all.
   *
   * @param request the request
   * @return the collection
   */
  default Collection<LdapEntry> findAll(@NotNull SearchRequest request) {
    return Optional.ofNullable(search(request))
        .map(SearchResponse::getEntries)
        .orElseGet(Collections::emptyList);
  }

  /**
   * Find all.
   *
   * @param <T> the type parameter
   * @param request the request
   * @param entryMapper the entry mapper
   * @return the stream
   */
  default <T> Stream<T> findAll(
      @NotNull SearchRequest request,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return Optional.ofNullable(search(request))
        .map(SearchResponse::getEntries)
        .orElseGet(Collections::emptyList)
        .stream()
        .map(entryMapper::map);
  }

  /**
   * Exists.
   *
   * @param dn the dn
   * @return the boolean
   */
  boolean exists(@NotNull String dn);

  /**
   * Exists.
   *
   * @param <T> the type parameter
   * @param domainObject the domain object
   * @param entryMapper the entry mapper
   * @return the boolean
   */
  default <T> boolean exists(@NotNull T domainObject, @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return exists(entryMapper.mapDn(domainObject));
  }

  /**
   * Save t.
   *
   * @param <T> the type parameter
   * @param domainObject the domain object
   * @param entryMapper the entry mapper
   * @return the t
   */
  <T> T save(T domainObject, LdaptiveEntryMapper<T> entryMapper);

  /**
   * Save all stream.
   *
   * @param <T> the type parameter
   * @param domainObjects the domain objects
   * @param entryMapper the entry mapper
   * @return the stream
   */
  default <T> Stream<T> saveAll(
      @Nullable Collection<T> domainObjects,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {

    return Optional.ofNullable(domainObjects)
        .map(col -> col.stream()
            .filter(Objects::nonNull)
            .map(domainObject -> save(domainObject, entryMapper)))
        .orElseGet(Stream::empty);
  }

  /**
   * Delete.
   *
   * @param <T> the type parameter
   * @param domainObject the domain object
   * @param entryMapper the entry mapper
   */
  default <T> void delete(
      @NotNull T domainObject,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {
    delete(DeleteRequest.builder().dn(entryMapper.mapDn(domainObject)).build());
  }

  /**
   * Delete all.
   *
   * @param <T> the type parameter
   * @param domainObjects the domain objects
   * @param entryMapper the entry mapper
   */
  default <T> void deleteAll(
      @Nullable Collection<T> domainObjects,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {
    Optional.ofNullable(domainObjects)
        .ifPresent(col -> col.forEach(domainObject -> delete(domainObject, entryMapper)));
  }

}
