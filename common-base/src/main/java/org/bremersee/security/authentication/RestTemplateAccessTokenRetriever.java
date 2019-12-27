package org.bremersee.security.authentication;

import java.io.IOException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bremersee.exception.PasswordFlowAuthenticationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class RestTemplateAccessTokenRetriever implements
    AccessTokenRetriever<String> {

  private RestTemplate restTemplate;

  public RestTemplateAccessTokenRetriever(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.restTemplate.setErrorHandler(new ErrorHandler());
  }

  @Override
  public String retrieveAccessToken(AccessTokenRetrieverProperties input) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    final HttpEntity<?> request = new HttpEntity<>(input.createBody(), headers);
    final String response = restTemplate.exchange(
        input.getTokenEndpoint(),
        HttpMethod.POST,
        request,
        String.class)
        .getBody();
    final JSONObject json = (JSONObject) JSONValue.parse(response);
    final String accessToken = json.getAsString("access_token");
    if (StringUtils.hasText(accessToken)) {
      return accessToken;
    }
    throw new PasswordFlowAuthenticationException(HttpStatus.UNAUTHORIZED,
        "There is no access token in the response: " + accessToken);
  }

  private static class ErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(final ClientHttpResponse response, final HttpStatus statusCode)
        throws IOException {
      final String statusText = response.getStatusText();
      throw new PasswordFlowAuthenticationException(statusCode, statusText);
    }
  }

}
