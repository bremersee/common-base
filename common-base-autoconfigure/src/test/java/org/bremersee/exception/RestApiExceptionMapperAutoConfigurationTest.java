package org.bremersee.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * The rest api exception mapper auto configuration test.
 */
class RestApiExceptionMapperAutoConfigurationTest {

  /**
   * Rest api exception mapper.
   */
  @Test
  void restApiExceptionMapper() {
    RestApiExceptionMapperAutoConfiguration configuration
        = new RestApiExceptionMapperAutoConfiguration(
        "app", new RestApiExceptionMapperProperties());
    configuration.init();
    assertNotNull(configuration.restApiExceptionMapper());
  }
}