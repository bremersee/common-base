/*
 * Copyright 2018 the original author or authors.
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

import java.util.function.Predicate;
import org.springframework.http.HttpStatus;

/**
 * A collection of error detectors.
 *
 * @author Christian Bremer
 */
public abstract class ErrorDetectors {

  private ErrorDetectors() {
  }

  /**
   * Detects errors with an http status code 4xx or 5xx.
   */
  public static final Predicate<HttpStatus> DEFAULT =
      httpStatus -> (httpStatus.series() == HttpStatus.Series.CLIENT_ERROR ||
          httpStatus.series() == HttpStatus.Series.SERVER_ERROR);

}
