package org.bremersee.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

public class AccessTokenRetrieverAuthenticationException extends AuthenticationException
    implements HttpStatusAware {

  private HttpStatus httpStatus;

  /**
   * Instantiates a new access token retriever authentication exception.
   *
   * @param httpStatus the http status
   * @param body       the body
   */
  public AccessTokenRetrieverAuthenticationException(
      final HttpStatus httpStatus, final String body) {
    super(buildMessage(httpStatus, body));
    this.httpStatus = httpStatus;
  }

  @Override
  public int status() {
    return httpStatus != null ? httpStatus.value() : HttpStatus.UNAUTHORIZED.value();
  }

  private static String buildMessage(final HttpStatus httpStatus, final String body) {
    if ((httpStatus == null || !StringUtils.hasText(httpStatus.getReasonPhrase()))
        && !StringUtils.hasText(body)) {
      return RestApiExceptionUtils.NO_MESSAGE_VALUE;
    }
    if (httpStatus == null) {
      return body;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Server returned status code ").append(httpStatus.value());
    if (StringUtils.hasText(httpStatus.getReasonPhrase())) {
      sb.append(" (").append(httpStatus.getReasonPhrase()).append(")");
    }
    if (StringUtils.hasText(body)) {
      sb.append(": ").append(body);
    }
    return sb.toString();
  }
}

