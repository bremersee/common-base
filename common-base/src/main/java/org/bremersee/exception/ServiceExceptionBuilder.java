package org.bremersee.exception;

import java.io.Serializable;

/**
 * The service exception builder.
 *
 * @param <T> the type parameter
 */
public interface ServiceExceptionBuilder<T extends ServiceException> extends Serializable {

  /**
   * Sets http status on service exception builder.
   *
   * @param httpStatus the http status
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> httpStatus(int httpStatus);

  /**
   * Sets error code on service exception builder.
   *
   * @param errorCode the error code
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> errorCode(String errorCode);

  /**
   * Sets reason on service exception builder.
   *
   * @param reason the reason
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> reason(String reason);

  /**
   * sets cause on service exception builder.
   *
   * @param cause the cause
   * @return the service exception builder
   */
  ServiceExceptionBuilder<T> cause(Throwable cause);

  /**
   * Build the service exception.
   *
   * @return the service exception
   */
  T build();

}
