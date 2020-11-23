package org.bremersee.data.ldaptive.reactive;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.bremersee.data.ldaptive.LdaptiveEntryMapper;
import org.ldaptive.AddRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Result;
import org.ldaptive.SearchRequest;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.PasswordModifyRequest;
import org.ldaptive.extended.PasswordModifyResponseParser;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The reactive ldaptive operations.
 *
 * @author Christian Bremer
 */
@Validated
public interface ReactiveLdaptiveOperations {

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
   * @return the mono
   */
  Mono<Result> add(@NotNull AddRequest addRequest);

  /**
   * Executes compare operation.
   *
   * @param compareRequest the compare request
   * @return the mono
   */
  Mono<Boolean> compare(@NotNull CompareRequest compareRequest);

  /**
   * Executes delete operation.
   *
   * @param deleteRequest the delete request
   * @return the mono
   */
  Mono<Result> delete(@NotNull DeleteRequest deleteRequest);

  /**
   * Executes extended request.
   *
   * @param request the request
   * @return the mono
   */
  Mono<ExtendedResponse> executeExtension(@NotNull ExtendedRequest request);

  /**
   * Generate user password.
   *
   * @param dn the dn
   * @return the mono
   */
  default Mono<String> generateUserPassword(@NotNull String dn) {
    return executeExtension(new PasswordModifyRequest(dn))
        .map(PasswordModifyResponseParser::parse);
  }

  /**
   * Executes modify operation.
   *
   * @param modifyRequest the modify request
   * @return the mono
   */
  Mono<Result> modify(@NotNull ModifyRequest modifyRequest);

  /**
   * Executes modify DN operation.
   *
   * @param modifyDnRequest the modify dn request
   * @return the mono
   */
  Mono<Result> modifyDn(@NotNull ModifyDnRequest modifyDnRequest);

  /**
   * Modify user password.
   *
   * @param dn the dn
   * @param oldPass the old pass
   * @param newPass the new pass
   * @return the mono
   */
  default Mono<ExtendedResponse> modifyUserPassword(
      @NotNull String dn,
      @Nullable String oldPass,
      @Nullable String newPass) {
    return executeExtension(new PasswordModifyRequest(dn, oldPass, newPass));
  }

  /**
   * Find one.
   *
   * @param searchRequest the search request
   * @return the mono
   */
  Mono<LdapEntry> findOne(@NotNull SearchRequest searchRequest);

  /**
   * Find one.
   *
   * @param <T> the type parameter
   * @param searchRequest the search request
   * @param entryMapper the entry mapper
   * @return the mono
   */
  default <T> Mono<T> findOne(@NotNull SearchRequest searchRequest, @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return findOne(searchRequest)
        .map(ldapEntry -> Objects.requireNonNull(entryMapper.map(ldapEntry)));
  }

  /**
   * Find all.
   *
   * @param searchRequest the search request
   * @return the flux
   */
  Flux<LdapEntry> findAll(@NotNull SearchRequest searchRequest);

  /**
   * Find all.
   *
   * @param <T> the type parameter
   * @param searchRequest the search request
   * @param entryMapper the entry mapper
   * @return the flux
   */
  default <T> Flux<T> findAll(@NotNull SearchRequest searchRequest, @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return findAll(searchRequest)
        .map(ldapEntry -> Objects.requireNonNull(entryMapper.map(ldapEntry)));
  }

  /**
   * Exists.
   *
   * @param dn the dn
   * @return the mono
   */
  default Mono<Boolean> exists(@NotNull String dn) {
    int index = dn.indexOf('=');
    if (index > 0) {
      String attr = dn.substring(0, index).trim();
      return findOne(SearchRequest.objectScopeSearchRequest(dn, new String[]{attr}))
          .map(ldapEntry -> true)
          .defaultIfEmpty(false);
    }
    return Mono.just(false);
  }

  /**
   * Exists.
   *
   * @param <T> the type parameter
   * @param domainObject the domain object
   * @param entryMapper the entry mapper
   * @return the mono
   */
  default <T> Mono<Boolean> exists(@NotNull T domainObject, @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return exists(entryMapper.mapDn(domainObject));
  }

  /**
   * Save.
   *
   * @param <T> the type parameter
   * @param domainObject the domain object
   * @param entryMapper the entry mapper
   * @return the mono
   */
  <T> Mono<T> save(@NotNull T domainObject, @NotNull LdaptiveEntryMapper<T> entryMapper);

  /**
   * Save all.
   *
   * @param <T> the type parameter
   * @param domainObjects the domain objects
   * @param entryMapper the entry mapper
   * @return the flux
   */
  default <T> Flux<T> saveAll(@Nullable Collection<T> domainObjects, @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return Flux.fromIterable(Optional.ofNullable(domainObjects).orElseGet(Collections::emptyList))
        .flatMap(domainObject -> save(domainObject, entryMapper));
  }

  /**
   * Remove.
   *
   * @param <T> the type parameter
   * @param domainObject the domain object
   * @param entryMapper the entry mapper
   * @return the mono
   */
  default <T> Mono<Result> remove(
      @NotNull T domainObject,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return delete(DeleteRequest.builder().dn(entryMapper.mapDn(domainObject)).build());
  }

  /**
   * Remove all.
   *
   * @param <T> the type parameter
   * @param domainObjects the domain objects
   * @param entryMapper the entry mapper
   * @return the mono
   */
  default <T> Mono<Long> removeAll(
      @Nullable Collection<T> domainObjects,
      @NotNull LdaptiveEntryMapper<T> entryMapper) {
    return Flux.fromIterable(Optional.ofNullable(domainObjects).orElseGet(Collections::emptyList))
        .flatMap(domainObject -> remove(domainObject, entryMapper))
        .count();
  }

}
