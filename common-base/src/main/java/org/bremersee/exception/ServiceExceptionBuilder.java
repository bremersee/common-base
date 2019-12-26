package org.bremersee.exception;

import java.io.Serializable;

public interface ServiceExceptionBuilder<T extends ServiceException> extends Serializable {

  ServiceExceptionBuilder<T> httpStatus(int httpStatus);

  ServiceExceptionBuilder<T> errorCode(String errorCode);

  ServiceExceptionBuilder<T> reason(String reason);

  ServiceExceptionBuilder<T> cause(Throwable cause);

  T build();

}
