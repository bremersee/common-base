/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.thymeleaf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bremersee.thymeleaf.AdditionalThymeleafProperties.ResolverProperties;
import org.junit.jupiter.api.Test;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * The thymeleaf resolver properties test.
 *
 * @author Christian Bremer
 */
public class ThymeleafResolverPropertiesTest {

  /**
   * Cacheable.
   */
  @Test
  void cacheable() {
    ResolverProperties expected = new ResolverProperties();
    expected.setCacheable(true);
    assertTrue(expected.isCacheable());

    ResolverProperties actual = new ResolverProperties();
    actual.setCacheable(true);

    assertEquals(expected, expected);
    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));

    assertNotEquals(expected, null);
    assertNotEquals(expected, new Object());
  }

  /**
   * Cacheable patterns.
   */
  @Test
  void cacheablePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setCacheablePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setCacheablePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Cache ttlms.
   */
  @Test
  void cacheTtlms() {
    ResolverProperties expected = new ResolverProperties();
    expected.setCacheTtlms(123456789L);

    ResolverProperties actual = new ResolverProperties();
    actual.setCacheTtlms(123456789L);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("123456789"));
  }

  /**
   * Character encoding.
   */
  @Test
  void characterEncoding() {
    String value = UUID.randomUUID().toString();

    ResolverProperties expected = new ResolverProperties();
    expected.setCharacterEncoding(value);

    ResolverProperties actual = new ResolverProperties();
    actual.setCharacterEncoding(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Check existence.
   */
  @Test
  void checkExistence() {
    ResolverProperties expected = new ResolverProperties();
    expected.setCheckExistence(true);
    assertTrue(expected.isCheckExistence());

    ResolverProperties actual = new ResolverProperties();
    actual.setCheckExistence(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Css template mode patterns.
   */
  @Test
  void cssTemplateModePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setCssTemplateModePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setCssTemplateModePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Force suffix.
   */
  @Test
  void forceSuffix() {
    ResolverProperties expected = new ResolverProperties();
    expected.setForceSuffix(true);
    assertTrue(expected.isForceSuffix());

    ResolverProperties actual = new ResolverProperties();
    actual.setForceSuffix(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Force template mode.
   */
  @Test
  void forceTemplateMode() {
    ResolverProperties expected = new ResolverProperties();
    expected.setForceTemplateMode(true);
    assertTrue(expected.isForceTemplateMode());

    ResolverProperties actual = new ResolverProperties();
    actual.setForceTemplateMode(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Html template mode patterns.
   */
  @Test
  void htmlTemplateModePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setHtmlTemplateModePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setHtmlTemplateModePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Java script template mode patterns.
   */
  @Test
  void javaScriptTemplateModePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setJavaScriptTemplateModePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setJavaScriptTemplateModePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Name.
   */
  @Test
  void name() {
    String value = UUID.randomUUID().toString();

    ResolverProperties expected = new ResolverProperties();
    expected.setName(value);

    ResolverProperties actual = new ResolverProperties();
    actual.setName(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Non cacheable patterns.
   */
  @Test
  void nonCacheablePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setNonCacheablePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setNonCacheablePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Prefix.
   */
  @Test
  void prefix() {
    String value = UUID.randomUUID().toString();

    ResolverProperties expected = new ResolverProperties();
    expected.setPrefix(value);

    ResolverProperties actual = new ResolverProperties();
    actual.setPrefix(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Raw template mode patterns.
   */
  @Test
  void rawTemplateModePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setRawTemplateModePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setRawTemplateModePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Resolvable patterns.
   */
  @Test
  void resolvablePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setResolvablePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setResolvablePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Suffix.
   */
  @Test
  void suffix() {
    String value = UUID.randomUUID().toString();

    ResolverProperties expected = new ResolverProperties();
    expected.setSuffix(value);

    ResolverProperties actual = new ResolverProperties();
    actual.setSuffix(value);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Template aliases.
   */
  @Test
  void templateAliases() {
    String value = UUID.randomUUID().toString();
    Map<String, String> values = Collections.singletonMap("key", value);

    ResolverProperties expected = new ResolverProperties();
    expected.setTemplateAliases(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setTemplateAliases(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Template mode.
   */
  @Test
  void templateMode() {
    ResolverProperties expected = new ResolverProperties();
    expected.setTemplateMode(TemplateMode.HTML);

    ResolverProperties actual = new ResolverProperties();
    actual.setTemplateMode(TemplateMode.HTML);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(TemplateMode.HTML.toString()));
  }

  /**
   * Text template mode patterns.
   */
  @Test
  void textTemplateModePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setTextTemplateModePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setTextTemplateModePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Use decoupled logic.
   */
  @Test
  void useDecoupledLogic() {
    ResolverProperties expected = new ResolverProperties();
    expected.setUseDecoupledLogic(true);
    assertTrue(expected.isUseDecoupledLogic());

    ResolverProperties actual = new ResolverProperties();
    actual.setUseDecoupledLogic(true);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains("true"));
  }

  /**
   * Xml template mode patterns.
   */
  @Test
  void xmlTemplateModePatterns() {
    String value = UUID.randomUUID().toString();
    Set<String> values = Collections.singleton(value);

    ResolverProperties expected = new ResolverProperties();
    expected.setXmlTemplateModePatterns(values);

    ResolverProperties actual = new ResolverProperties();
    actual.setXmlTemplateModePatterns(values);

    assertEquals(expected, actual);
    assertTrue(expected.toString().contains(value));
  }

  /**
   * Resolvable patterns or default.
   */
  @Test
  void resolvablePatternsOrDefault() {
    ResolverProperties model = new ResolverProperties();
    model.setResolvablePatterns(Collections.emptySet());

    Set<String> pattern = model.resolvablePatternsOrDefault();
    assertNotNull(pattern);
    assertEquals(1, pattern.size());
    assertEquals("*", new ArrayList<>(pattern).get(0));

    model = new ResolverProperties();
    String value0 = UUID.randomUUID().toString();
    String value1 = UUID.randomUUID().toString();
    model.getResolvablePatterns().add(value0);
    model.getResolvablePatterns().add(value1);
    pattern = model.resolvablePatternsOrDefault();
    assertEquals(2, pattern.size());
    assertTrue(pattern.contains(value0));
    assertTrue(pattern.contains(value1));
  }

}
