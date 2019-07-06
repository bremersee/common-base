package org.bremersee.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * The error detectors test.
 *
 * @author Christian Bremer
 */
public class ErrorDetectorsTest {

  /**
   * Tests default error detectors.
   */
  @Test
  public void defaultErrorDetectors() {
    assertTrue(ErrorDetectors.DEFAULT.test(HttpStatus.INTERNAL_SERVER_ERROR));
    assertTrue(ErrorDetectors.DEFAULT.test(HttpStatus.NOT_FOUND));
    assertFalse(ErrorDetectors.DEFAULT.test(HttpStatus.OK));
  }

}