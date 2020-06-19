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

package org.bremersee.web;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

/**
 * The request parameter.
 *
 * @author Christian Bremer
 */
@Getter
@ToString
@EqualsAndHashCode
@SuppressWarnings("FieldMayBeFinal")
public class ReqParam {

  private String name;

  private boolean required;

  /**
   * Instantiates a new request parameter.
   *
   * @param name the parameter name
   */
  public ReqParam(String name) {
    this(name, false);
  }

  /**
   * Instantiates a new request parameter.
   *
   * @param name the parameter name
   * @param required specifies whether the multi part of the content part name must be present
   *     or not
   */
  @Builder(toBuilder = true)
  public ReqParam(String name, boolean required) {
    Assert.notNull(name, "Request parameter name is required.");
    this.name = name;
    this.required = required;
  }
}
