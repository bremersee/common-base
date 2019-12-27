package org.bremersee.web.reactive.function.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.bremersee.security.authentication.AccessTokenRetriever;
import org.bremersee.security.authentication.PasswordFlowProperties;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The jwt authentication token appender test.
 *
 * @author Christian Bremer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AccessTokenAppenderWithRetrieverTest {

  private static AccessTokenAppender appender = AccessTokenAppender.fromAuthentication();

  private static ClientRequest request = ClientRequest
      .create(HttpMethod.GET, URI.create("http://localhost/resource"))
      .header("Accept", "*/*")
      .build();

  /**
   * Tests filter.
   */
  @Test
  public void filter() {
    //noinspection unchecked
    AccessTokenRetriever<Mono<String>> tokenRetriever = mock(
        AccessTokenRetriever.class);
    when(tokenRetriever.retrieveAccessToken(any(PasswordFlowProperties.class)))
        .thenReturn(Mono.just(validAccessToken()));

    AccessTokenAppender appender = AccessTokenAppender.withAccessTokenRetriever(
        tokenRetriever,
        properties());

    ExchangeFunction exchangeFunction = createExchangeFunction();
    StepVerifier.create(appender.filter(request, exchangeFunction))
        .assertNext(response -> {
          assertEquals(HttpStatus.OK, response.statusCode());
          StepVerifier.create(response.bodyToMono(String.class))
              .assertNext(body -> assertEquals("It works", body))
              .expectNextCount(0)
              .verifyComplete();
          verify(exchangeFunction, times(1)).exchange(any(ClientRequest.class));
        })
        .expectNextCount(0)
        .verifyComplete();
  }

  /**
   * Tests filter failure.
   */
  @Test
  public void filterFails() {
    //noinspection unchecked
    AccessTokenRetriever<Mono<String>> tokenRetriever = mock(
        AccessTokenRetriever.class);
    when(tokenRetriever.retrieveAccessToken(any(PasswordFlowProperties.class)))
        .thenReturn(Mono.just(invalidAccessToken()));

    AccessTokenAppender appender = AccessTokenAppender.withAccessTokenRetriever(
        tokenRetriever,
        properties());

    ExchangeFunction exchangeFunction = createExchangeFunction();
    StepVerifier.create(appender.filter(request, exchangeFunction))
        .assertNext(response -> {
          assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode());
          verify(exchangeFunction, times(1)).exchange(any(ClientRequest.class));
        })
        .expectNextCount(0)
        .verifyComplete();
  }

  private static ExchangeFunction createExchangeFunction() {
    ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .then((Answer<Mono<ClientResponse>>) invocationOnMock -> {
          ClientRequest req = invocationOnMock.getArgument(0);
          ClientResponse res;
          if (req.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            res = ClientResponse.create(HttpStatus.OK)
                .body("It works")
                .build();
          } else {
            res = ClientResponse.create(HttpStatus.UNAUTHORIZED).build();
          }
          return Mono.just(res);
        });
    return exchangeFunction;
  }

  private static PasswordFlowProperties properties() {
    return PasswordFlowProperties.builder()
        .clientId("clientId")
        .clientSecret("clientSecret")
        .username("systemUser")
        .password("systemPass")
        .tokenEndpoint("http://localhost/token")
        .build();
  }

  private static String validAccessToken() {
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .audience("http://audience")
        .expirationTime(new Date(System.currentTimeMillis() + 1000L * 60L * 30L))
        .issuer("http://issuer")
        .issueTime(new Date())
        .jwtID(UUID.randomUUID().toString())
        .notBeforeTime(new Date(System.currentTimeMillis() - 1000L))
        .subject(UUID.randomUUID().toString())
        .build();
    return new PlainJWT(claimsSet).serialize();
  }

  private static String invalidAccessToken() {
    return "";
  }

}