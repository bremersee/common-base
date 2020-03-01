/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.thymeleaf.templateresource.ITemplateResource;

/**
 * The template resource test.
 *
 * @author Christian Bremer
 */
class TemplateResourceTest {

  /**
   * Gets description.
   */
  @Test
  void getDescription() {
    TemplateResource templateResource = new TemplateResource(
        "classpath:templates/mail/invitation.html",
        StandardCharsets.UTF_8.name(),
        new DefaultResourceLoader());
    assertEquals("classpath:templates/mail/invitation.html", templateResource.getDescription());
  }

  /**
   * Gets base name.
   */
  @Test
  void getBaseName() {
    TemplateResource templateResource = new TemplateResource(
        "classpath:templates/mail/invitation.html",
        new DefaultResourceLoader());
    assertEquals("invitation", templateResource.getBaseName());
  }

  /**
   * Exists.
   */
  @Test
  void exists() {
    TemplateResource templateResource = new TemplateResource(
        "classpath:templates/mail/invitation.html",
        StandardCharsets.UTF_8.name());
    assertTrue(templateResource.exists());

    templateResource = new TemplateResource("classpath:templates/mail/cancellation.html");
    assertFalse(templateResource.exists());
  }

  /**
   * Reader.
   *
   * @throws IOException the io exception
   */
  @Test
  void reader() throws IOException {
    TemplateResource templateResource = new TemplateResource(
        "classpath:templates/mail/invitation.html");
    assertTrue(templateResource.exists());
    try (Reader reader = templateResource.reader()) {
      assertNotNull(reader);
    }
  }

  /**
   * Relative.
   */
  @Test
  void relative() {
    TemplateResource templateResource = new TemplateResource(
        "classpath:templates/mail/invitation.html");
    ITemplateResource relativeResource = templateResource.relative("../foo.html");
    assertNotNull(relativeResource);
    assertEquals("classpath:templates/mail/../foo.html", relativeResource.getDescription());
  }
}