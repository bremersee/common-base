package org.bremersee.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import org.junit.Test;

/**
 * The service exception test.
 *
 * @author Christian Bremer
 */
public class ServiceExceptionTest {

  /**
   * Tests status method.
   */
  @Test
  public void status() {
    ServiceException serviceException = new ServiceException();
    assertEquals(0, serviceException.status());

    serviceException = new ServiceException(404, null);
    assertEquals(404, serviceException.status());
  }

  /**
   * Tests get error code.
   */
  @Test
  public void getErrorCode() {
    ServiceException serviceException = new ServiceException();
    assertNull(serviceException.getErrorCode());

    serviceException = new ServiceException(0, "abc");
    assertEquals("abc", serviceException.getErrorCode());
  }

  /**
   * Internal server error.
   */
  @Test
  public void internalServerError() {
    ServiceException serviceException = ServiceException.internalServerError();
    assertNull(serviceException.getMessage());
    assertEquals(500, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String reason = "Some reason";
    serviceException = ServiceException.internalServerError(reason);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(500, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final Exception cause = new Exception();
    serviceException = ServiceException.internalServerError(reason, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(cause, serviceException.getCause());
    assertEquals(500, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "TEST:4711";
    serviceException = ServiceException.internalServerError(reason, errorCode);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(500, serviceException.status());

    serviceException = ServiceException.internalServerError(reason, errorCode, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(cause, serviceException.getCause());
    assertEquals(500, serviceException.status());
  }

  /**
   * Bad request.
   */
  @Test
  public void badRequest() {
    ServiceException serviceException = ServiceException.badRequest();
    assertNull(serviceException.getMessage());
    assertEquals(400, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String reason = "Some reason";
    serviceException = ServiceException.badRequest(reason);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(400, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final Exception cause = new Exception();
    serviceException = ServiceException.badRequest(reason, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(cause, serviceException.getCause());
    assertEquals(400, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "TEST:4711";
    serviceException = ServiceException.badRequest(reason, errorCode);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(400, serviceException.status());

    serviceException = ServiceException.badRequest(reason, errorCode, cause);
    assertEquals(reason, serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(cause, serviceException.getCause());
    assertEquals(400, serviceException.status());
  }

  /**
   * Not found.
   */
  @Test
  public void notFound() {
    ServiceException serviceException = ServiceException.notFound();
    assertNull(serviceException.getMessage());
    assertEquals(404, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final Object entityName = UUID.randomUUID();
    serviceException = ServiceException.notFound(entityName);
    assertEquals(
        String.format("Entity with identifier [%s] was not found.", entityName),
        serviceException.getMessage());
    assertEquals(404, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String entityType = "Person";
    serviceException = ServiceException.notFound(entityType, entityName);
    assertEquals(
        String.format("%s with identifier [%s] was not found.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(404, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "NF:5678";
    serviceException = ServiceException.notFoundWithErrorCode(entityName, errorCode);
    assertEquals(
        String.format("Entity with identifier [%s] was not found.", entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(404, serviceException.status());

    serviceException = ServiceException.notFoundWithErrorCode(entityType, entityName, errorCode);
    assertEquals(
        String.format("%s with identifier [%s] was not found.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(404, serviceException.status());
  }

  /**
   * Already exists.
   */
  @Test
  public void alreadyExists() {
    ServiceException serviceException = ServiceException.alreadyExists();
    assertNull(serviceException.getMessage());
    assertEquals(409, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final Object entityName = UUID.randomUUID();
    serviceException = ServiceException.alreadyExists(entityName);
    assertEquals(
        String.format("Entity with identifier [%s] already exists.", entityName),
        serviceException.getMessage());
    assertEquals(409, serviceException.status());
    assertEquals(ServiceException.ERROR_CODE_ALREADY_EXISTS, serviceException.getErrorCode());

    final String entityType = "Person";
    serviceException = ServiceException.alreadyExists(entityType, entityName);
    assertEquals(
        String.format("%s with identifier [%s] already exists.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(409, serviceException.status());
    assertEquals(ServiceException.ERROR_CODE_ALREADY_EXISTS, serviceException.getErrorCode());

    final String errorCode = "NF:5678";
    serviceException = ServiceException.alreadyExistsWithErrorCode(entityName, errorCode);
    assertEquals(
        String.format("Entity with identifier [%s] already exists.", entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(409, serviceException.status());

    serviceException = ServiceException
        .alreadyExistsWithErrorCode(entityType, entityName, errorCode);
    assertEquals(
        String.format("%s with identifier [%s] already exists.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(409, serviceException.status());
  }

  /**
   * Forbidden.
   */
  @Test
  public void forbidden() {
    ServiceException serviceException = ServiceException.forbidden();
    assertNull(serviceException.getMessage());
    assertEquals(403, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final Object entityName = UUID.randomUUID();
    serviceException = ServiceException.forbidden(entityName);
    assertEquals(
        String.format("Access to entity with identifier [%s] is forbidden.", entityName),
        serviceException.getMessage());
    assertEquals(403, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String entityType = "Person";
    serviceException = ServiceException.forbidden(entityType, entityName);
    assertEquals(
        String.format("Access to [%s] with identifier [%s] is forbidden.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(403, serviceException.status());
    assertNull(serviceException.getErrorCode());

    final String errorCode = "NF:5678";
    serviceException = ServiceException.forbiddenWithErrorCode(errorCode);
    assertNull(serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(403, serviceException.status());

    serviceException = ServiceException.forbiddenWithErrorCode(entityName, errorCode);
    assertEquals(
        String.format("Access to entity with identifier [%s] is forbidden.", entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(403, serviceException.status());

    serviceException = ServiceException.forbiddenWithErrorCode(entityType, entityName, errorCode);
    assertEquals(
        String.format("Access to [%s] with identifier [%s] is forbidden.", entityType, entityName),
        serviceException.getMessage());
    assertEquals(errorCode, serviceException.getErrorCode());
    assertEquals(403, serviceException.status());
  }

}