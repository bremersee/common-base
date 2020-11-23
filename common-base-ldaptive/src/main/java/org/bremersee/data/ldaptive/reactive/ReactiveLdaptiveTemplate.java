package org.bremersee.data.ldaptive.reactive;

import static org.ldaptive.handler.ResultPredicate.NOT_SUCCESS;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.DefaultLdaptiveErrorHandler;
import org.bremersee.data.ldaptive.LdaptiveEntryMapper;
import org.bremersee.data.ldaptive.LdaptiveErrorHandler;
import org.bremersee.data.ldaptive.LdaptiveTemplate;
import org.bremersee.exception.ServiceException;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
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
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.extended.ExtendedOperation;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * The reactive ldaptive template.
 *
 * @author Christian Bremer
 */
@Slf4j
public class ReactiveLdaptiveTemplate implements ReactiveLdaptiveOperations {

  private static final ResultPredicate NOT_COMPARE_RESULT = result -> !result.isSuccess()
      && result.getResultCode() != ResultCode.COMPARE_TRUE
      && result.getResultCode() != ResultCode.COMPARE_FALSE;

  private static final ResultPredicate NOT_DELETE_RESULT = result -> !result.isSuccess()
      && result.getResultCode() != ResultCode.NO_SUCH_OBJECT;

  private static final ResultPredicate NOT_FIND_RESULT = NOT_DELETE_RESULT;

  private final ConnectionFactory connectionFactory;

  private LdaptiveErrorHandler errorHandler = new DefaultLdaptiveErrorHandler();

  /**
   * Instantiates a new Reactive ldaptive template.
   *
   * @param connectionFactory the connection factory
   */
  public ReactiveLdaptiveTemplate(ConnectionFactory connectionFactory) {
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
  public void setErrorHandler(LdaptiveErrorHandler errorHandler) {
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
  public ReactiveLdaptiveTemplate clone() {
    return clone(null);
  }

  /**
   * Returns a new instance of this ldaptive template with the same connection factory and the given error handler.
   *
   * @param errorHandler the new error handler
   * @return the new instance of the ldaptive template
   */
  public ReactiveLdaptiveTemplate clone(final LdaptiveErrorHandler errorHandler) {
    final ReactiveLdaptiveTemplate template = new ReactiveLdaptiveTemplate(connectionFactory);
    template.setErrorHandler(errorHandler);
    return template;
  }

  @Override
  public Mono<Result> add(AddRequest addRequest) {
    CompletableFuture<Result> future = new CompletableFuture<>();
    try {
      AddOperation.builder()
          .factory(connectionFactory)
          .onResult(new FutureAwareResultHandler<>(future, NOT_SUCCESS, errorHandler, r -> r))
          .onException(ldapException -> future.completeExceptionally(errorHandler.map(ldapException)))
          .build()
          .send(addRequest);

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  private <T> Mono<T> add(T domainObject, LdaptiveEntryMapper<T> entryMapper) {
    String[] objectClasses = entryMapper.getObjectClasses();
    if (objectClasses == null || objectClasses.length == 0) {
      final ServiceException se = ServiceException.internalServerError(
          "Object classes must be specified to save a new ldap entry.",
          "org.bremersee:common-base-ldaptive:d7aa5699-fd2e-45df-a863-97960e8095b8");
      log.error("Saving domain object failed.", se);
      throw se;
    }
    String dn = entryMapper.mapDn(domainObject);
    LdapEntry entry = new LdapEntry();
    entryMapper.map(domainObject, entry);
    entry.setDn(dn);
    entry.addAttributes(new LdapAttribute("objectclass", objectClasses));
    return add(new AddRequest(dn, entry.getAttributes()))
        .then(Mono.just(Objects.requireNonNull(entryMapper.map(entry))));
  }

  @Override
  public Mono<Boolean> bind(BindRequest bindRequest) {
    // Bind requests are synchronous
    LdaptiveTemplate template = new LdaptiveTemplate(getConnectionFactory());
    template.setErrorHandler(errorHandler);
    return Mono.just(template.bind(bindRequest));
  }

  @Override
  public Mono<Boolean> compare(CompareRequest compareRequest) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    try {
      CompareOperation.builder()
          .factory(connectionFactory)
          .onCompare(future::complete) // this will be only called, if the result is COMPARE_TRUE or COMPARE_FALSE
          .onResult(new FutureAwareResultHandler<>(future, NOT_COMPARE_RESULT, errorHandler, Result::isSuccess))
          .onException(ldapException -> future.completeExceptionally(errorHandler.map(ldapException)))
          .build()
          .send(compareRequest);

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  @Override
  public Mono<Result> delete(DeleteRequest deleteRequest) {
    CompletableFuture<Result> future = new CompletableFuture<>();
    try {
      DeleteOperation.builder()
          .factory(connectionFactory)
          .onResult(new FutureAwareResultHandler<>(future, NOT_DELETE_RESULT, errorHandler, r -> r))
          .onException(ldapException -> future.completeExceptionally(errorHandler.map(ldapException)))
          .build()
          .send(deleteRequest)
          .await();

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  @Override
  public Mono<ExtendedResponse> executeExtension(ExtendedRequest request) {

    CompletableFuture<ExtendedResponse> future = new CompletableFuture<>();
    try {
      ExtendedOperation.builder()
          .factory(connectionFactory)
          .onExtended((name, value) -> future.complete(ExtendedResponse.builder()
              .responseName(name)
              .responseValue(value)
              .resultCode(ResultCode.SUCCESS)
              .build()))
          .onResult(new FutureAwareResultHandler<>(
              future,
              NOT_SUCCESS,
              errorHandler,
              r -> ExtendedResponse.builder().resultCode(r.getResultCode()).build()))
          .onException(ldapException -> future.completeExceptionally(errorHandler.map(ldapException)))
          .build()
          .send(request);

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  @Override
  public Mono<Result> modify(ModifyRequest modifyRequest) {
    CompletableFuture<Result> future = new CompletableFuture<>();
    try {
      ModifyOperation.builder()
          .factory(connectionFactory)
          .onResult(new FutureAwareResultHandler<>(future, NOT_SUCCESS, errorHandler, r -> r))
          .onException(ldapException -> future.completeExceptionally(errorHandler.map(ldapException)))
          .build()
          .send(modifyRequest);

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  private <T> Mono<T> modify(T domainObject, LdapEntry entry, LdaptiveEntryMapper<T> entryMapper) {
    String dn = entryMapper.mapDn(domainObject);
    AttributeModification[] modifications = entryMapper.mapAndComputeModifications(domainObject, entry);
    return modify(new ModifyRequest(dn, modifications))
        .then(Mono.just(Objects.requireNonNull(entryMapper.map(entry))));
  }

  @Override
  public Mono<Result> modifyDn(ModifyDnRequest modifyDnRequest) {
    CompletableFuture<Result> future = new CompletableFuture<>();
    try {
      ModifyDnOperation.builder()
          .factory(connectionFactory)
          .onResult(new FutureAwareResultHandler<>(future, NOT_SUCCESS, errorHandler, r -> r))
          .onException(ldapException -> future.completeExceptionally(errorHandler.map(ldapException)))
          .build()
          .send(modifyDnRequest);

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  @Override
  public Mono<LdapEntry> findOne(SearchRequest searchRequest) {
    CompletableFuture<LdapEntry> future = new CompletableFuture<>();
    try {
      SearchOperation.builder()
          .factory(connectionFactory)
          .onEntry(ldapEntry -> {
            future.complete(ldapEntry);
            return ldapEntry;
          })
          .onResult(new FutureAwareResultHandler<>(future, NOT_FIND_RESULT, errorHandler, null))
          .onException(ldapException -> future.obtrudeException(errorHandler.map(ldapException)))
          .build()
          .send(searchRequest);

    } catch (LdapException e) {
      future.completeExceptionally(errorHandler.map(e));
    }
    return Mono.fromFuture(future);
  }

  @Override
  public Flux<LdapEntry> findAll(SearchRequest searchRequest) {
    return Flux.create((FluxSink<LdapEntry> fluxSink) -> {
      try {
        SearchOperation.builder()
            .factory(connectionFactory)
            .onEntry(ldapEntry -> {
              fluxSink.next(ldapEntry);
              return ldapEntry;
            })
            .onResult(new FluxSinkAwareResultHandler<>(fluxSink, NOT_FIND_RESULT, errorHandler))
            .onException(ldapException -> fluxSink.error(errorHandler.map(ldapException)))
            .build()
            .send(searchRequest);

      } catch (LdapException e) {
        fluxSink.error(errorHandler.map(e));
      }
    });
  }

  @Override
  public <T> Mono<T> save(T domainObject, LdaptiveEntryMapper<T> entryMapper) {
    return findOne(SearchRequest.objectScopeSearchRequest(entryMapper.mapDn(domainObject)))
        .flatMap(entry -> modify(domainObject, entry, entryMapper))
        .switchIfEmpty(add(domainObject, entryMapper));
  }

  private static class FutureAwareResultHandler<T> implements ResultHandler {

    private final CompletableFuture<T> future;

    private final ResultPredicate throwErrorPredicate;

    private final LdaptiveErrorHandler errorHandler;

    private final Function<Result, T> resultValueFn;

    /**
     * Instantiates a new Future aware result handler.
     *
     * @param future the future
     * @param throwErrorPredicate the throw error predicate
     * @param errorHandler the error handler
     * @param resultValueFn the result value fn
     */
    public FutureAwareResultHandler(
        CompletableFuture<T> future,
        ResultPredicate throwErrorPredicate,
        LdaptiveErrorHandler errorHandler,
        Function<Result, T> resultValueFn) {
      this.throwErrorPredicate = throwErrorPredicate;
      this.errorHandler = errorHandler;
      this.future = future;
      this.resultValueFn = resultValueFn;
    }

    @Override
    public void accept(Result result) {
      if (!future.isDone()) {
        if (throwErrorPredicate != null && throwErrorPredicate.test(result)) {
          future.completeExceptionally(errorHandler.map(new LdapException(result)));
        } else {
          future.complete(resultValueFn != null ? resultValueFn.apply(result) : null);
        }
      }
    }
  }

  private static class FluxSinkAwareResultHandler<T> implements ResultHandler {

    private final FluxSink<T> fluxSink;

    private final ResultPredicate throwErrorPredicate;

    private final LdaptiveErrorHandler errorHandler;

    /**
     * Instantiates a new Flux sink aware result handler.
     *
     * @param fluxSink the flux sink
     * @param throwErrorPredicate the throw error predicate
     * @param errorHandler the error handler
     */
    public FluxSinkAwareResultHandler(
        FluxSink<T> fluxSink,
        ResultPredicate throwErrorPredicate,
        LdaptiveErrorHandler errorHandler) {
      this.throwErrorPredicate = throwErrorPredicate;
      this.errorHandler = errorHandler;
      this.fluxSink = fluxSink;
    }

    @Override
    public void accept(Result result) {
      if (throwErrorPredicate != null && throwErrorPredicate.test(result)) {
        fluxSink.error(errorHandler.map(new LdapException(result)));
      } else {
        fluxSink.complete();
      }
    }
  }

}
