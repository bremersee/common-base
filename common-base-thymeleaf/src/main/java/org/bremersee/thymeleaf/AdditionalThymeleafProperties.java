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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * The additional thymeleaf properties.
 *
 * @author Christian Bremer
 */
// @ConfigurationProperties("bremersee.thymeleaf") // TODO
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AdditionalThymeleafProperties {

  private Integer resolverStartIndex = 2;

  private List<ResolverProperties> resolvers = new ArrayList<>();

  /**
   * The additional thymeleaf resolver properties.
   */
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  public static class ResolverProperties {

    private boolean cacheable = false;

    private Set<String> cacheablePatterns = new LinkedHashSet<>();

    private Long cacheTtlms;

    private String characterEncoding = StandardCharsets.UTF_8.name();

    private boolean checkExistence = false;

    private Set<String> cssTemplateModePatterns = new LinkedHashSet<>();

    private boolean forceSuffix = false;

    private boolean forceTemplateMode = false;

    private Set<String> htmlTemplateModePatterns = new LinkedHashSet<>();

    private Set<String> javaScriptTemplateModePatterns = new LinkedHashSet<>();

    private String name;

    private Set<String> nonCacheablePatterns = new LinkedHashSet<>();

    private Integer order;

    private String prefix = "classpath:"; // important

    private Set<String> rawTemplateModePatterns = new LinkedHashSet<>();

    private Set<String> resolvablePatterns = new LinkedHashSet<>(); // important

    private String suffix = ".html";  // important

    private Map<String, String> templateAliases = new LinkedHashMap<>();

    private TemplateMode templateMode;

    private Set<String> textTemplateModePatterns = new LinkedHashSet<>();

    private boolean useDecoupledLogic = false;

    private Set<String> xmlTemplateModePatterns = new LinkedHashSet<>();

    /**
     * Resolvable patterns or default.
     *
     * @return the resolvable patterns
     */
    public Set<String> resolvablePatternsOrDefault() {
      if (resolvablePatterns.isEmpty()) {
        return Collections.singleton("*");
      }
      return resolvablePatterns;
    }

  }

}
