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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.thymeleaf.templateresource.ITemplateResource;

/**
 * The template resource.
 *
 * @author Christian Bremer
 */
@Slf4j
public class TemplateResource implements ITemplateResource {

  private final ResourceLoader resourceLoader;

  private final String path;

  private final String characterEncoding;

  /**
   * Instantiates a new template resource.
   *
   * @param path the path
   */
  @SuppressWarnings("unused")
  public TemplateResource(String path) {
    this(path, null, null);
  }

  /**
   * Instantiates a new template resource.
   *
   * @param path              the path
   * @param characterEncoding the character encoding
   */
  @SuppressWarnings("unused")
  public TemplateResource(
      String path,
      String characterEncoding) {
    this(path, characterEncoding, null);
  }

  /**
   * Instantiates a new template resource.
   *
   * @param path           the path
   * @param resourceLoader the resource loader
   */
  @SuppressWarnings("unused")
  public TemplateResource(
      String path,
      ResourceLoader resourceLoader) {
    this(path, null, resourceLoader);
  }

  /**
   * Instantiates a new template resource.
   *
   * @param path              the path
   * @param characterEncoding the character encoding
   * @param resourceLoader    the resource loader
   */
  public TemplateResource(
      String path,
      String characterEncoding,
      ResourceLoader resourceLoader) {
    this.path = path;
    this.characterEncoding = StringUtils.hasText(characterEncoding)
        ? characterEncoding
        : StandardCharsets.UTF_8.name();
    this.resourceLoader = resourceLoader != null ? resourceLoader : new DefaultResourceLoader();
    log.info("TemplateResource (path={}, characterEncoding={})",
        this.path, this.characterEncoding);
  }

  @Override
  public String getDescription() {
    return path;
  }

  @Override
  public String getBaseName() {
    if (path == null || path.length() == 0) {
      return null;
    }
    final String basePath = (path.charAt(path.length() - 1) == '/'
        ? path.substring(0, path.length() - 1)
        : path);
    final int slashPos = basePath.lastIndexOf('/');
    if (slashPos != -1) {
      final int dotPos = basePath.lastIndexOf('.');
      if (dotPos != -1 && dotPos > slashPos + 1) {
        return basePath.substring(slashPos + 1, dotPos);
      }
      return basePath.substring(slashPos + 1);
    } else {
      final int dotPos = basePath.lastIndexOf('.');
      if (dotPos != -1) {
        return basePath.substring(0, dotPos);
      }
    }
    return (basePath.length() > 0 ? basePath : null);
  }

  @Override
  public boolean exists() {
    return resourceLoader.getResource(path).exists();
  }

  @Override
  public Reader reader() throws IOException {
    return new BufferedReader(
        new InputStreamReader(
            new BufferedInputStream(
                resourceLoader.getResource(path).getInputStream()), characterEncoding));
  }

  @Override
  public ITemplateResource relative(String relativeLocation) {
    Assert.hasText(relativeLocation, "Relative Path cannot be null or empty.");
    final String newPath = computeRelativeLocation(path, relativeLocation);
    return new TemplateResource(newPath, characterEncoding, resourceLoader);
  }

  private static String computeRelativeLocation(
      final String location,
      final String relativeLocation) {

    final int separatorPos = location.lastIndexOf('/');
    if (separatorPos != -1) {
      final StringBuilder relativeBuilder = new StringBuilder(
          location.length() + relativeLocation.length());
      relativeBuilder.append(location, 0, separatorPos);
      if (relativeLocation.charAt(0) != '/') {
        relativeBuilder.append('/');
      }
      relativeBuilder.append(relativeLocation);
      return relativeBuilder.toString();
    }
    return relativeLocation;
  }

}
