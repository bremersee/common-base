package org.bremersee.security.reactive.function.client;

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
import org.bremersee.security.OAuth2Properties;
import org.bremersee.security.OAuth2Properties.PasswordFlowProperties;
import org.bremersee.security.authentication.AccessTokenRetriever;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The password flow token appender test.
 *
 * @author Christian Bremer
 */
public class PasswordFlowTokenAppenderTest {

  /**
   * Tests filter.
   */
  @Test
  public void filter() {
    //noinspection unchecked
    AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> tokenRetriever = mock(
        AccessTokenRetriever.class);
    //noinspection unchecked
    when(tokenRetriever.retrieveAccessToken(any(MultiValueMap.class)))
        .thenReturn(Mono.just(validAccessToken()));

    PasswordFlowTokenAppender tokenAppender = tokenAppender(tokenRetriever);

    ClientRequest request = ClientRequest
        .create(HttpMethod.GET, URI.create("http://localhost/resource"))
        .header("Accept", "*/*")
        .build();

    ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
        .body("It works")
        .build();

    ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(clientResponse));

    // first call
    StepVerifier.create(tokenAppender.filter(request, exchangeFunction))
        .assertNext(response -> {
          assertEquals(HttpStatus.OK, response.statusCode());
          StepVerifier.create(response.bodyToMono(String.class))
              .assertNext(body -> assertEquals("It works", body))
              .expectNextCount(0)
              .verifyComplete();
          //noinspection unchecked
          verify(tokenRetriever, times(1))
              .retrieveAccessToken(any(MultiValueMap.class));
        })
        .expectNextCount(0)
        .verifyComplete();

    // second call
    StepVerifier.create(tokenAppender.filter(request, exchangeFunction))
        .assertNext(response -> {
          assertEquals(HttpStatus.OK, response.statusCode());
          StepVerifier.create(response.bodyToMono(String.class))
              .assertNext(body -> assertEquals("It works", body))
              .expectNextCount(0)
              .verifyComplete();
          //noinspection unchecked
          verify(tokenRetriever, times(1)) // we use cache, it remains one
              .retrieveAccessToken(any(MultiValueMap.class));
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
    AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> tokenRetriever = mock(
        AccessTokenRetriever.class);
    //noinspection unchecked
    when(tokenRetriever.retrieveAccessToken(any(MultiValueMap.class)))
        .thenReturn(Mono.just(invalidAccessTokern()));

    PasswordFlowTokenAppender tokenAppender = tokenAppender(tokenRetriever);

    ClientRequest request = ClientRequest
        .create(HttpMethod.GET, URI.create("http://localhost/resource"))
        .header("Accept", "*/*")
        .build();

    ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
        .body("It works")
        .build();

    ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(clientResponse));

    StepVerifier.create(tokenAppender.filter(request, exchangeFunction))
        .expectError(JwtException.class)
        .verify();
  }

  private static PasswordFlowTokenAppender tokenAppender(
      AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> tokenRetriever) {
    PasswordFlowProperties passwordFlowProperties = new PasswordFlowProperties();
    passwordFlowProperties.setClientId("clientId");
    passwordFlowProperties.setClientSecret("clientSecret");
    passwordFlowProperties.setExpirationTimeRemainsMillis(3000L);
    passwordFlowProperties.setSystemPassword("systemPass");
    passwordFlowProperties.setSystemUsername("systemUser");
    passwordFlowProperties.setTokenEndpoint("http://localhost/token");
    OAuth2Properties properties = new OAuth2Properties();
    properties.setPasswordFlow(passwordFlowProperties);
    return new PasswordFlowTokenAppender(properties, tokenRetriever);
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

  private static String invalidAccessTokern() {
    return "foobar";
  }

}