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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

/**
 * The template for executing ldap operations.
 *
 * @author Christian Bremer
 */
@Slf4j
public class LdaptiveTemplate implements LdaptiveOperations {

  private final ConnectionFactory connectionFactory;

  /**
   * Instantiates a new ldap template.
   *
   * @param connectionFactory the connection factory
   */
  public LdaptiveTemplate(final ConnectionFactory connectionFactory) {
    Assert.notNull(connectionFactory, "Connection factory must not be null.");
    this.connectionFactory = connectionFactory;
  }

  private Connection getConnection() throws LdapException {
    final Connection connection = this.connectionFactory.getConnection();
    if (!connection.isOpen()) {
      connection.open();
    }
    return connection;
  }

  /**
   * Close the given context and ignore any thrown exception. This is useful for typical finally
   * blocks in manual ldap statements.
   *
   * @param connection the ldap connection to close
   */
  private void closeConnection(final Connection connection) {
    if (connection != null && connection.isOpen()) {
      try {
        connection.close();
      } catch (final Exception ex) {
        log.warn("Closing ldap connection failed.", ex);
      }
    }
  }

  @Override
  public <T> T execute(final LdaptiveConnectionCallback<T> callback) {
    Connection connection = null;
    try {
      connection = getConnection();
      return callback.doWithConnection(connection);
    } catch (final LdapRuntimeException e) {
      final ServiceException serviceException = new ServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "org.bremersee:base-common-ldaptive:8150d733-cfb2-46a9-94f6-4f3395e7cecf",
          e.getLdapException());
      log.error("Executing ldap operation failed.", serviceException);
      throw serviceException;
    } catch (final LdapException e) {
      final ServiceException serviceException = new ServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "org.bremersee:base-common-ldaptive:af8cb1fc-f9c2-4592-8bd6-de59c6f2a8e6",
          e);
      log.error("Executing ldap operation failed.", serviceException);
      throw serviceException;
    } finally {
      closeConnection(connection);
    }
  }

  /**
   * Execute the given add request.
   *
   * @param addRequest the add request
   */
  public void add(@NotNull final AddRequest addRequest) {
    execute(connection -> new AddOperation(connection).execute(addRequest));
  }

  /**
   * Execute the given modify request.
   *
   * @param modifyRequest the modify request
   */
  public void modify(@NotNull final ModifyRequest modifyRequest) {
    if (modifyRequest.getAttributeModifications() != null
        && modifyRequest.getAttributeModifications().length > 0) {
      execute(connection -> new ModifyOperation(connection).execute(modifyRequest));
    }
  }

  /**
   * Execute the given delete request.
   *
   * @param deleteRequest the delete request
   */
  public void delete(@NotNull final DeleteRequest deleteRequest) {
    execute((LdaptiveConnectionCallbackWithoutResult) connection -> new DeleteOperation(connection)
        .execute(deleteRequest));
  }

  /**
   * Find one ldap entry.
   *
   * @param searchRequest the search request
   * @return the ldap entry
   */
  public Optional<LdapEntry> findOne(
      @NotNull final SearchRequest searchRequest) {

    return Optional.ofNullable(
        execute(connection -> new SearchOperation(connection)
            .execute(searchRequest)
            .getResult()
            .getEntry()));
  }

  /**
   * Find one domain object.
   *
   * @param <T>           the type of the mapped domain object
   * @param searchRequest the search request
   * @param entryMapper   the entry mapper that maps a ldap entry into the domain object
   * @return the domain object
   */
  public <T> Optional<T> findOne(
      @NotNull final SearchRequest searchRequest,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {

    return Optional.ofNullable(
        execute(connection -> entryMapper
            .map(new SearchOperation(connection)
                .execute(searchRequest)
                .getResult()
                .getEntry())));
  }

  /**
   * Find all ldap entries.
   *
   * @param searchRequest the search request
   * @return the ldap entries
   */
  public Collection<LdapEntry> findAll(
      @NotNull final SearchRequest searchRequest) {

    return execute(connection -> new SearchOperation(connection)
        .execute(searchRequest)
        .getResult()
        .getEntries());
  }

  /**
   * Find domain objects.
   *
   * @param <T>           the type of the domain objects
   * @param searchRequest the search request
   * @param entryMapper   the entry mapper that maps a ldap entry into the domain object
   * @return the stream of found domain objects
   */
  public <T> Stream<T> findAll(
      @NotNull final SearchRequest searchRequest,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {

    return execute(connection -> new SearchOperation(connection)
        .execute(searchRequest)
        .getResult()
        .getEntries()
        .stream()
        .map(entryMapper::map))
        .filter(Objects::nonNull);
  }

  /**
   * Check whether a domain object exists or not.
   *
   * @param <T>          the type of the domain object
   * @param domainObject the domain object
   * @param entryMapper  the entry mapper that maps a ldap entry into the domain object
   * @return {@code true} if the domain object exists, otherwise {@code false}
   */
  public <T> boolean exists(
      @NotNull final T domainObject,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {
    return execute(connection -> {
      try {
        return entryMapper.map(new SearchOperation(connection)
            .execute(SearchRequest.newObjectScopeSearchRequest(entryMapper.mapDn(domainObject)))
            .getResult()
            .getEntry()) != null;

      } catch (LdapException e) {
        if (e.getCause() instanceof javax.naming.NameNotFoundException) {
          return false;
        }
        throw e;
      }
    });
  }

  private <T> T save(
      final T domainObject,
      final LdaptiveEntryMapper<T> entryMapper,
      final Connection connection) throws LdapRuntimeException {

    try {
      final String dn = entryMapper.mapDn(domainObject);
      LdapEntry destination = new SearchOperation(connection)
          .execute(SearchRequest.newObjectScopeSearchRequest(dn))
          .getResult()
          .getEntry();
      if (destination == null) {
        destination = new LdapEntry();
        entryMapper.map(domainObject, destination);
        new AddOperation(connection)
            .execute(new AddRequest(dn, destination.getAttributes()));
      } else {
        final AttributeModification[] modifications = entryMapper
            .mapAndComputeModifications(domainObject, destination);
        if (modifications != null && modifications.length > 0) {
          new ModifyOperation(connection)
              .execute(new ModifyRequest(dn, modifications));
        }
      }
      return entryMapper.map(destination);

    } catch (LdapException e) {
      throw new LdapRuntimeException(e);
    }
  }

  /**
   * Save domain object.
   *
   * @param <T>          the type of the domain object
   * @param domainObject the domain object
   * @param entryMapper  the entry mapper that maps a ldap entry into the domain object
   * @return the saved domain object
   */
  public <T> T save(
      @NotNull final T domainObject,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {
    return execute(connection -> save(domainObject, entryMapper, connection));
  }

  /**
   * Save all domain objects.
   *
   * @param <T>          the type of the domain objects
   * @param domainModels the domain objects
   * @param entryMapper  the entry mapper that maps a ldap entry into the domain object
   * @return the stream of saved domain objects
   */
  public <T> Stream<T> saveAll(
      final Collection<T> domainModels,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {
    if (domainModels == null || domainModels.isEmpty()) {
      return Stream.empty();
    }
    return execute(connection -> domainModels
        .stream()
        .filter(Objects::nonNull)
        .map(domainModel -> save(domainModel, entryMapper, connection)));
  }

  private <T> void delete(
      final T domainModel,
      final LdaptiveEntryMapper<T> entryMapper,
      final Connection connection) throws LdapRuntimeException {

    try {
      new DeleteOperation(connection).execute(new DeleteRequest(entryMapper.mapDn(domainModel)));

    } catch (LdapException e) {
      throw new LdapRuntimeException(e);
    }
  }

  /**
   * Delete domain object.
   *
   * @param <T>          the type of the domain object
   * @param domainObject the domain object
   * @param entryMapper  the entry mapper that maps a ldap entry into the domain object
   */
  public <T> void delete(
      @NotNull final T domainObject,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {
    execute(
        (LdaptiveConnectionCallbackWithoutResult) connection -> delete(
            domainObject, entryMapper, connection));
  }

  /**
   * Delete all domain objects.
   *
   * @param <T>           the type of the domain objects
   * @param domainObjects the domain objects
   * @param entryMapper   the entry mapper that maps a ldap entry into the domain object
   */
  public <T> void deleteAll(
      final Collection<T> domainObjects,
      @NotNull final LdaptiveEntryMapper<T> entryMapper) {
    if (domainObjects != null) {
      execute((LdaptiveConnectionCallbackWithoutResult) connection -> {
        for (T domainModel : domainObjects) {
          if (domainModel != null) {
            delete(domainModel, entryMapper, connection);
          }
        }
      });
    }
  }

  private static class LdapRuntimeException extends RuntimeException {

    @Getter(AccessLevel.PACKAGE)
    private LdapException ldapException;

    /**
     * Instantiates a new Ldap runtime exception.
     *
     * @param ldapException the ldap exception
     */
    LdapRuntimeException(LdapException ldapException) {
      super(ldapException);
      this.ldapException = ldapException;
    }
  }

}
