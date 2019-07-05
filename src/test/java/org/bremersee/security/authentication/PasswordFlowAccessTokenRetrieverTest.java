package org.bremersee.security.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.PasswordFlowAuthenticationException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * The password flow access token retriever test.
 *
 * @author Christian Bremer
 */
public class PasswordFlowAccessTokenRetrieverTest {

  /**
   * Retrieve access token.
   */
  @Test
  public void retrieveAccessToken() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
        anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
        .thenReturn(ResponseEntity.ok("{\"access_token\":\"junit_access_token_value\"}"));
    RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    PasswordFlowAccessTokenRetriever tokenRetriever = new PasswordFlowAccessTokenRetriever(
        restTemplateBuilder, "http://localhost/token");

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    String token = tokenRetriever.retrieveAccessToken(body);
    Assert.assertNotNull(token);
    Assert.assertEquals("junit_access_token_value", token);
  }

  /**
   * Retrieve access token fails.
   */
  @Test(expected = PasswordFlowAuthenticationException.class)
  public void retrieveAccessTokenFails() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
        anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
        .thenReturn(ResponseEntity.ok("{\"illegal_token\":\"junit_access_token_value\"}"));
    RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    PasswordFlowAccessTokenRetriever tokenRetriever = new PasswordFlowAccessTokenRetriever(
        restTemplateBuilder, "http://localhost/token");

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    tokenRetriever.retrieveAccessToken(body);
  }

}