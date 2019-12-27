package org.bremersee.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.AccessTokenRetrieverAuthenticationException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * The password flow access token retriever test.
 *
 * @author Christian Bremer
 */
public class RestTemplateAccessTokenRetrieverTest {

  /**
   * Retrieve access token.
   */
  @Test
  public void retrieveAccessToken() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    //noinspection unchecked
    when(restTemplate.exchange(
        anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
        .thenReturn(ResponseEntity.ok("{\"access_token\":\"junit_access_token_value\"}"));
    RestTemplateAccessTokenRetriever tokenRetriever = new RestTemplateAccessTokenRetriever(
        restTemplate);

    PasswordFlowProperties properties = PasswordFlowProperties.builder()
        .tokenEndpoint("http://localhost/token")
        .clientId("123")
        .clientSecret("456")
        .username("789")
        .password("012")
        .build();
    String token = tokenRetriever.retrieveAccessToken(properties);
    assertNotNull(token);
    assertEquals("junit_access_token_value", token);
  }

  /**
   * Retrieve access token fails.
   */
  @Test(expected = AccessTokenRetrieverAuthenticationException.class)
  public void retrieveAccessTokenFails() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    //noinspection unchecked
    when(restTemplate.exchange(
        anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
        .thenReturn(ResponseEntity.ok("{\"illegal_token\":\"junit_access_token_value\"}"));
    RestTemplateAccessTokenRetriever tokenRetriever = new RestTemplateAccessTokenRetriever(
        restTemplate);

    PasswordFlowProperties properties = PasswordFlowProperties.builder()
        .tokenEndpoint("http://localhost/token")
        .clientId("123")
        .clientSecret("456")
        .username("789")
        .password("012")
        .build();
    tokenRetriever.retrieveAccessToken(properties);
  }

}