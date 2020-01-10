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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresource.ITemplateResource;

/**
 * The type Template resolver test.
 *
 * @author Christian Bremer
 */
class TemplateResolverTest {

  /**
   * Compute template resource.
   */
  @Test
  void computeTemplateResource() {
    TemplateResolver templateResolver = new TemplateResolver();
    templateResolver.setPrefix("classpath:template/mail/");
    templateResolver.setSuffix(".html");
    templateResolver.setResolvablePatterns(Collections.singleton("*"));
    templateResolver.setTemplateMode(TemplateMode.HTML);

    ITemplateResource templateResource = templateResolver.computeTemplateResource(
        null,
        "ignored",
        "ignored",
        "classpath:template/mail/invitation.html",
        StandardCharsets.UTF_8.name(),
        null);

    assertNotNull(templateResolver);
    assertEquals("classpath:template/mail/invitation.html", templateResource.getDescription());
  }
}