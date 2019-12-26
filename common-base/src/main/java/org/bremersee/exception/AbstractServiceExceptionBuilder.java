package org.bremersee.exception;

import org.springframework.util.StringUtils;

public abstract class AbstractServiceExceptionBuilder<T extends ServiceException> implements
    ServiceExceptionBuilder<T> {

  private static final long serialVersionUID = 2L;

  protected int httpStatus;

  protected String reason;

  protected String errorCode;

  protected Throwable cause;

  @Override
  public ServiceExceptionBuilder<T> httpStatus(int httpStatus) {
    this.httpStatus = httpStatus;
    return this;
  }

  @Override
  public ServiceExceptionBuilder<T> errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  @Override
  public ServiceExceptionBuilder<T> reason(String reason) {
    this.reason = reason;
    return this;
  }

  @Override
  public ServiceExceptionBuilder<T> cause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  @Override
  public T build() {
    if (StringUtils.hasText(reason) && cause != null) {
      return buildWith(httpStatus, errorCode, reason, cause);
    }
    if (!StringUtils.hasText(reason) && cause != null) {
      return buildWith(httpStatus, errorCode, cause);
    }
    if (StringUtils.hasText(reason) && cause == null) {
      return buildWith(httpStatus, errorCode, reason);
    }
    return buildWith(httpStatus, errorCode);
  }

  protected abstract T buildWith(int httpStatus, String errorCode);

  protected abstract T buildWith(int httpStatus, String errorCode, String reason);

  protected abstract T buildWith(int httpStatus, String errorCode, Throwable cause);

  protected abstract T buildWith(int httpStatus, String errorCode, String reason, Throwable cause);

}
