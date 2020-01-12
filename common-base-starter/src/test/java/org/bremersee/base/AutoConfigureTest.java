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

package org.bremersee.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.base.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.MessageSource;

/**
 * The auto configure test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "bremersee.messages.fallback-to-system-locale=false",
        "bremersee.messages.default-locale=en",
        "bremersee.messages.base-names=i18n"
    })
@Slf4j
public class AutoConfigureTest {

  /**
   * The message source.
   */
  @Autowired
  MessageSource messageSource;

  /**
   * Test message source.
   */
  @Test
  void testMessageSource() {
    String actual = messageSource.getMessage("test0", null, Locale.ENGLISH);
    assertEquals("Hello JUnit", actual);

    actual = messageSource.getMessage("test1", new Object[]{"Anna"}, Locale.ENGLISH);
    assertEquals("Hello Anna", actual);

    actual = messageSource.getMessage("test0", null, Locale.GERMAN);
    assertEquals("Hallo JUnit", actual);

    actual = messageSource.getMessage("test1", new Object[]{"Anna"}, Locale.GERMAN);
    assertEquals("Hallo Anna", actual);

    actual = messageSource.getMessage("test0", null, Locale.FRANCE);
    assertEquals("Bon jour JUnit", actual);

    actual = messageSource.getMessage("test1", new Object[]{"Anna"}, Locale.FRANCE);
    assertEquals("Bon jour Anna", actual);
  }
}
