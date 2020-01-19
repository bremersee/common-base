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

package org.bremersee.test.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

/**
 * The rest api tester path.
 *
 * @author Christian Bremer
 */
public interface RestApiTesterPath {

  /**
   * Creates a new path builder.
   *
   * @return the path builder
   */
  static RestApiTesterPath.Builder pathBuilder() {
    return new BuilderImpl();
  }

  /**
   * To path builder.
   *
   * @return the path builder
   */
  RestApiTesterPath.Builder toPathBuilder();

  /**
   * The path builder interface.
   */
  interface Builder {

    /**
     * Add step and value.
     *
     * @param stepType the step type
     * @param value the value
     * @return the builder
     */
    Builder add(PathType stepType, String value);

    /**
     * Build rest api tester path.
     *
     * @return the rest api tester path
     */
    RestApiTesterPath build();
  }

  /**
   * The rest api tester path implementation.
   */
  class Impl extends ArrayList<RestApiTesterPathValues> implements RestApiTesterPath {

    private Impl(Collection<? extends RestApiTesterPathValues> collection) {
      super(collection);
    }

    @Override
    public RestApiTesterPath.Builder toPathBuilder() {
      return new BuilderImpl(this);
    }

    @Override
    public String toString() {
      return StringUtils.collectionToDelimitedString(this, " -> ");
    }
  }

  /**
   * The path builder implementation.
   */
  class BuilderImpl implements RestApiTesterPath.Builder {

    private List<RestApiTesterPathValues> values = new ArrayList<>();

    private BuilderImpl() {
    }

    private BuilderImpl(Collection<? extends RestApiTesterPathValues> collection) {
      if (collection != null) {
        values.addAll(collection);
      }
    }

    @Override
    public Builder add(PathType stepType, String value) {
      values.add(new RestApiTesterPathValues(stepType, value));
      return this;
    }

    @Override
    public RestApiTesterPath build() {
      return new Impl(new ArrayList<>(values));
    }
  }

  /**
   * The path type.
   */
  enum PathType {

    /**
     * Path type Class.
     */
    CLASS("Class"),

    /**
     * Path type Method.
     */
    METHOD("Method"),

    /**
     * Path type Method parameter.
     */
    METHOD_PARAMETER("Parameter"),

    /**
     * Path type Annotation.
     */
    ANNOTATION("Annotation"),

    /**
     * Path type Attribute.
     */
    ATTRIBUTE("Attribute");

    private String value;

    PathType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  /**
   * The path values.
   */
  @EqualsAndHashCode(doNotUseGetters = true)
  class RestApiTesterPathValues {

    private PathType type;

    private String value;

    /**
     * Instantiates a new path model.
     *
     * @param type the type
     * @param value the value
     */
    RestApiTesterPathValues(PathType type, String value) {
      this.type = type;
      this.value = value;
    }

    @Override
    public String toString() {
      return type + " (" + value + ")";
    }
  }
}
