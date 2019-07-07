package org.bremersee.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * The service exception test.
 *
 * @author Christian Bremer
 */
public class ServiceExceptionTest {

  /**
   * Tests supplier method.
   */
  @Test
  public void get() {
    ServiceException serviceException = new ServiceException();
    assertEquals(serviceException, serviceException.get());
  }

  /**
   * Tests status method.
   */
  @Test
  public void status() {
    ServiceException serviceException = new ServiceException();
    assertNull(serviceException.status());

    serviceException = new ServiceException(HttpStatus.NOT_FOUND);
    assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND.value()), serviceException.status());
  }

  /**
   * Tests get http status code.
   */
  @Test
  public void getHttpStatusCode() {
    ServiceException serviceException = new ServiceException();
    assertNull(serviceException.getHttpStatusCode());

    serviceException = new ServiceException(HttpStatus.NOT_FOUND);
    assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND.value()),
        serviceException.getHttpStatusCode());
  }

  /**
   * Tests get error code.
   */
  @Test
  public void getErrorCode() {
    ServiceException serviceException = new ServiceException();
    assertNull(serviceException.getErrorCode());

    serviceException = new ServiceException(HttpStatus.NOT_FOUND, "abc");
    assertEquals("abc", serviceException.getErrorCode());
  }

  /**
   * Internal server error.
   */
  @Test
  public void internalServerError() {
    ServiceException serviceException = ServiceException.internalServerError();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String reason = "Some reason";
    serviceException = ServiceException.internalServerError(reason);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final Exception cause = new Exception();
    serviceException = ServiceException.internalServerError(reason, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(cause, serviceException.getCause());
    assertEquals(
        Integer.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "TEST:4711";
    serviceException = ServiceException.internalServerError(reason, errorCode);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        serviceException.getHttpStatusCode());

    serviceException = ServiceException.internalServerError(reason, errorCode, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(cause, serviceException.getCause());
    assertEquals(
        Integer.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        serviceException.getHttpStatusCode());
  }

  /**
   * Bad request.
   */
  @Test
  public void badRequest() {
    ServiceException serviceException = ServiceException.badRequest();
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.BAD_REQUEST.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String reason = "Some reason";
    serviceException = ServiceException.badRequest(reason);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.BAD_REQUEST.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final Exception cause = new Exception();
    serviceException = ServiceException.badRequest(reason, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(cause, serviceException.getCause());
    assertEquals(
        Integer.valueOf(HttpStatus.BAD_REQUEST.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "TEST:4711";
    serviceException = ServiceException.badRequest(reason, errorCode);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.BAD_REQUEST.value()),
        serviceException.getHttpStatusCode());

    serviceException = ServiceException.badRequest(reason, errorCode, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(cause, serviceException.getCause());
    assertEquals(
        Integer.valueOf(HttpStatus.BAD_REQUEST.value()),
        serviceException.getHttpStatusCode());
  }

  /**
   * Not found.
   */
  @Test
  public void notFound() {
    ServiceException serviceException = ServiceException.notFound();
    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.NOT_FOUND.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final Object entityName = UUID.randomUUID();
    serviceException = ServiceException.notFound(entityName);
    assertEquals(
        String.format("Entity with identifier [%s] was not found.", entityName),
        serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.NOT_FOUND.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String entityType = "Person";
    serviceException = ServiceException.notFound(entityType, entityName);
    assertEquals(
        String.format("%s with identifier [%s] was not found.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.NOT_FOUND.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "NF:5678";
    serviceException = ServiceException.notFoundWithErrorCode(entityName, errorCode);
    assertEquals(
        String.format("Entity with identifier [%s] was not found.", entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.NOT_FOUND.value()),
        serviceException.getHttpStatusCode());

    serviceException = ServiceException.notFoundWithErrorCode(entityType, entityName, errorCode);
    assertEquals(
        String.format("%s with identifier [%s] was not found.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.NOT_FOUND.value()),
        serviceException.getHttpStatusCode());
  }

  /**
   * Already exists.
   */
  @Test
  public void alreadyExists() {
    ServiceException serviceException = ServiceException.alreadyExists();
    assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.CONFLICT.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final Object entityName = UUID.randomUUID();
    serviceException = ServiceException.alreadyExists(entityName);
    assertEquals(
        String.format("Entity with identifier [%s] already exists.", entityName),
        serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.CONFLICT.value()),
        serviceException.getHttpStatusCode());
    assertEquals(ServiceException.ERROR_CODE_ALREADY_EXISTS, serviceException.getErrorCode());

    final String entityType = "Person";
    serviceException = ServiceException.alreadyExists(entityType, entityName);
    assertEquals(
        String.format("%s with identifier [%s] already exists.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.CONFLICT.value()),
        serviceException.getHttpStatusCode());
    assertEquals(ServiceException.ERROR_CODE_ALREADY_EXISTS, serviceException.getErrorCode());

    final String errorCode = "NF:5678";
    serviceException = ServiceException.alreadyExistsWithErrorCode(entityName, errorCode);
    assertEquals(
        String.format("Entity with identifier [%s] already exists.", entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.CONFLICT.value()),
        serviceException.getHttpStatusCode());

    serviceException = ServiceException
        .alreadyExistsWithErrorCode(entityType, entityName, errorCode);
    assertEquals(
        String.format("%s with identifier [%s] already exists.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.CONFLICT.value()),
        serviceException.getHttpStatusCode());
  }

  /**
   * Forbidden.
   */
  @Test
  public void forbidden() {
    ServiceException serviceException = ServiceException.forbidden();
    assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.FORBIDDEN.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final Object entityName = UUID.randomUUID();
    serviceException = ServiceException.forbidden(entityName);
    assertEquals(
        String.format("Access to entity with identifier [%s] is forbidden.", entityName),
        serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.FORBIDDEN.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String entityType = "Person";
    serviceException = ServiceException.forbidden(entityType, entityName);
    assertEquals(
        String.format("Access to [%s] with identifier [%s] is forbidden.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(
        Integer.valueOf(HttpStatus.FORBIDDEN.value()),
        serviceException.getHttpStatusCode());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "NF:5678";
    serviceException = ServiceException.forbiddenWithErrorCode(entityName, errorCode);
    assertEquals(
        String.format("Access to entity with identifier [%s] is forbidden.", entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.FORBIDDEN.value()),
        serviceException.getHttpStatusCode());

    serviceException = ServiceException.forbiddenWithErrorCode(entityType, entityName, errorCode);
    assertEquals(
        String.format("Access to [%s] with identifier [%s] is forbidden.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(
        Integer.valueOf(HttpStatus.FORBIDDEN.value()),
        serviceException.getHttpStatusCode());
  }

}