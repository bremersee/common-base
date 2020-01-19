package org.bremersee.http.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * The type Object mapper helper test.
 */
class ObjectMapperHelperTest {

  /**
   * Gets json mapper.
   */
  @Test
  void getJsonMapper() {
    assertNotNull(ObjectMapperHelper.getJsonMapper());
  }

  /**
   * Gets xml mapper.
   */
  @Test
  void getXmlMapper() {
    assertNotNull(ObjectMapperHelper.getXmlMapper());
  }
}