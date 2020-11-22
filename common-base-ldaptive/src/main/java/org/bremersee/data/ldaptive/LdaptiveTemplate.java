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

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.BindOperation;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Operation;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.extended.ExtendedOperation;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.handler.ResultPredicate;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

/**
 * The template for executing ldap operations.
 *
 * @author Christian Bremer
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
public class LdaptiveTemplate implements LdaptiveOperations, Cloneable {

  private final ConnectionFactory connectionFactory;

  private ErrorHandler errorHandler = new DefaultLdaptiveErrorHandler();

  /**
   * Instantiates a new ldap template.
   *
   * @param connectionFactory the connection factory
   */
  public LdaptiveTemplate(ConnectionFactory connectionFactory) {
    Assert.notNull(connectionFactory, "Connection factory must not be null.");
    this.connectionFactory = connectionFactory;
  }

  @Override
  public ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  /**
   * Sets error handler.
   *
   * @param errorHandler the error handler
   */
  public void setErrorHandler(final ErrorHandler errorHandler) {
    if (errorHandler != null) {
      this.errorHandler = errorHandler;
    }
  }

  /**
   * Returns a new instance of this ldaptive template with the same connection factory and error handler.
   *
   * @return a new instance of this ldaptive template
   */
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public LdaptiveTemplate clone() {
    return clone(null);
  }

  /**
   * Returns a new instance of this ldaptive template with the same connection factory and the given error handler.
   *
   * @param errorHandler the new error handler
   * @return the new instance of the ldaptive template
   */
  public LdaptiveTemplate clone(final ErrorHandler errorHandler) {
    final LdaptiveTemplate template = new LdaptiveTemplate(connectionFactory);
    template.setErrorHandler(errorHandler);
    return template;
  }

  private <Q extends Request, S extends Result> S execute(Operation<Q, S> operation, Q request) {
    try {
      return operation.execute(request);

    } catch (Exception e) {
      errorHandler.handleError(e);
      return null;
    }
  }

  @Override
  public void add(AddRequest request) {
    execute(
        AddOperation.builder()
            .factory(getConnectionFactory())
            .throwIf(ResultPredicate.NOT_SUCCESS)
            .build(),
        request);
  }

  @Override
  public boolean bind(BindRequest request) {
    return Optional.ofNullable(execute(new BindOperation(getConnectionFactory()), request))
        .map(BindResponse::isSuccess)
        .orElse(false);
  }

  @Override
  public boolean compare(CompareRequest request) {
    return Optional.ofNullable(execute(new CompareOperation(getConnectionFactory()), request))
        .map(CompareResponse::isTrue)
        .orElse(false);
  }

  @Override
  public void delete(DeleteRequest request) {
    execute(
        DeleteOperation.builder()
            .factory(getConnectionFactory())
            .throwIf(result -> result.getResultCode() != ResultCode.SUCCESS
                && result.getResultCode() != ResultCode.NO_SUCH_OBJECT)
            .build(),
        request);
  }

  @Override
  public ExtendedResponse executeExtension(ExtendedRequest request) {
    return execute(
        ExtendedOperation.builder()
            .factory(getConnectionFactory())
            .throwIf(ResultPredicate.NOT_SUCCESS)
            .build(),
        request);
  }

  @Override
  public void modify(ModifyRequest request) {
    if (request.getModifications() != null && request.getModifications().length > 0) {
      execute(
          ModifyOperation.builder()
              .factory(getConnectionFactory())
              .throwIf(ResultPredicate.NOT_SUCCESS)
              .build(),
          request);
    }
  }

  @Override
  public void modifyDn(ModifyDnRequest request) {
    execute(
        ModifyDnOperation.builder()
            .factory(getConnectionFactory())
            .throwIf(ResultPredicate.NOT_SUCCESS)
            .build(),
        request);
  }

  @Override
  public SearchResponse search(SearchRequest request) {
    return execute(
        SearchOperation.builder()
            .factory(getConnectionFactory())
            .throwIf(ResultPredicate.NOT_SUCCESS)
            .build(),
        request);
  }

  @Override
  public boolean exists(String dn) {
    try {
      SearchResponse response = SearchOperation.builder()
          .factory(getConnectionFactory())
          .throwIf(result -> result.getResultCode() != ResultCode.NO_SUCH_OBJECT
              && result.getResultCode() != ResultCode.SUCCESS)
          .build()
          .execute(SearchRequest.objectScopeSearchRequest(dn));
      return response.isSuccess();

    } catch (LdapException e) {
      errorHandler.handleError(e);
      return false;
    }
  }

  @Override
  public <T> T save(T domainObject, LdaptiveEntryMapper<T> entryMapper) {

    String dn = entryMapper.mapDn(domainObject);
    SearchResponse searchResponse = execute(
        SearchOperation.builder()
            .factory(getConnectionFactory())
            .throwIf(result -> result.getResultCode() != ResultCode.NO_SUCH_OBJECT
                && result.getResultCode() != ResultCode.SUCCESS)
            .build(),
        SearchRequest.objectScopeSearchRequest(dn));
    return Optional.ofNullable(searchResponse)
        .map(SearchResponse::getEntry)
        .map(entry -> {
          AttributeModification[] modifications = entryMapper.mapAndComputeModifications(domainObject, entry);
          modify(new ModifyRequest(dn, modifications));
          return entryMapper.map(entry);
        })
        .orElseGet(() -> {
          String[] objectClasses = entryMapper.getObjectClasses();
          if (objectClasses == null || objectClasses.length == 0) {
            final ServiceException se = ServiceException.internalServerError(
                "Object classes must be specified to save a new ldap entry.",
                "org.bremersee:common-base-ldaptive:d7aa5699-fd2e-45df-a863-97960e8095b8");
            log.error("Saving domain object failed.", se);
            throw se;
          }
          LdapEntry entry = new LdapEntry();
          entryMapper.map(domainObject, entry);
          entry.setDn(dn);
          entry.addAttributes(new LdapAttribute("objectclass", objectClasses));
          add(new AddRequest(dn, entry.getAttributes()));
          return entryMapper.map(entry);
        });
  }

}
