package org.bremersee.test.web;

import org.junit.Test;

/**
 * The rst api annotation tester test.
 */
public class RestApiAnnotationTesterTest {

  /**
   * Compare good apis.
   */
  @Test
  public void compareGoodApis() {
    RestApiAnnotationTester.assertSameApiAnnotations(GoodRestApiOne.class, GoodRestApiTwo.class);
  }

  /**
   * Compare bad apis and expect wrong class annotations.
   */
  @Test(expected = AssertionError.class)
  public void compareBadApisAndExpectWrongClassAnnotations() {
    RestApiAnnotationTester.assertSameApiAnnotations(BadApis.One.class, BadApis.Two.class);
  }

  /**
   * Compare bad apis and expect wrong size of methods.
   */
  @Test(expected = AssertionError.class)
  public void compareBadApisAndExpectWrongSizeOfMethods() {
    RestApiAnnotationTester.assertSameApiAnnotations(BadApis.Three.class, BadApis.Four.class);
  }

  /**
   * Compare bad apis and expect wrong methods.
   */
  @Test(expected = AssertionError.class)
  public void compareBadApisAndExpectWrongMethods() {
    RestApiAnnotationTester.assertSameApiAnnotations(BadApis.Five.class, BadApis.Six.class);
  }

  /**
   * Compare bad apis and expect wrong method parameters.
   */
  @Test(expected = AssertionError.class)
  public void compareBadApisAndExpectWrongMethodParameters() {
    RestApiAnnotationTester.assertSameApiAnnotations(BadApis.Seven.class, BadApis.Eight.class);
  }

}