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

package org.bremersee.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;

/**
 * Configuration properties for the rest api exception handler / resolver.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.exception-mapping")
@Data
public class RestApiExceptionMapperProperties {

  /**
   * The request paths the handler is responsible for. Default is empty.
   */
  private List<String> apiPaths = new ArrayList<>();

  /**
   * The default values of a rest api exception that will be set, if a value is not detected. The
   * default values are:
   * <table style="border: 1px solid">
   * <thead>
   * <tr>
   * <th style="border: 1px solid">attribute</th>
   * <th style="border: 1px solid">value</th>
   * </tr>
   * </thead>
   * <tbody>
   * <tr>
   * <td style="border: 1px solid">status</td>
   * <td style="border: 1px solid">500</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">message</td>
   * <td style="border: 1px solid">Internal Server Error</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">code</td>
   * <td style="border: 1px solid">UNSPECIFIED</td>
   * </tr>
   * </tbody>
   * </table>
   */
  private ExceptionMapping defaultExceptionMapping;

  /**
   * Values ​​of errors whose values ​​can not be determined automatically. The name of the
   * exception can be a package, too (e. g. org.bremersee.foobar.*).
   *
   * <p>Examples application.yml:
   * <pre>
   * bremersee:
   *   exception-mapping:
   *     exception-mappings:
   *     - exception-class-name: org.springframework.security.access.AccessDeniedException
   *       status: 403
   *       message: Forbidden
   *       code: XYZ:0815
   *     - exception-class-name: javax.persistence.EntityNotFoundException
   *       status: 404
   *       message: Not Found
   *       code: GEN:404
   * </pre>
   */
  private List<ExceptionMapping> exceptionMappings = new ArrayList<>();

  /**
   * The default configuration of the exception mapping.
   *
   * <table style="border: 1px solid">
   * <thead>
   * <tr>
   * <th style="border: 1px solid">attribute</th>
   * <th style="border: 1px solid">value</th>
   * </tr>
   * </thead>
   * <tbody>
   * <tr>
   * <td style="border: 1px solid">includeExceptionClassName</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeApplicationName</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includePath</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeHandler</td>
   * <td style="border: 1px solid">false</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeStackTrace</td>
   * <td style="border: 1px solid">false</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeCause</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">evaluateAnnotationFirst</td>
   * <td style="border: 1px solid">false</td>
   * </tr>
   * </tbody>
   * </table>
   *
   * <p>If two values of a key are available (e. g. per annotation (see {@link
   * org.springframework.web.bind.annotation.ResponseStatus}) and member attribute) it is possible
   * with {@code evaluateAnnotationFirst} to specify which one should be used.
   */
  private ExceptionMappingConfig defaultExceptionMappingConfig;

  /**
   * Specifies mapping configuration per exception class.  The name of the exception can be a
   * package, too (e. g. org.bremersee.foobar.*).
   *
   * <p>Examples application.yml:
   * <pre>
   * bremersee:
   *   exception-mapping:
   *     exception-mapping-configs:
   *     - exception-class-name: org.springframework.security.access.AccessDeniedException
   *       include-exception-class-name: false
   *       include-handler: true
   *       include-cause: true
   *     - exception-class-name: org.springframework.*
   *       include-exception-class-name: true
   *       include-handler: true
   *       include-cause: false
   * </pre>
   */
  private List<ExceptionMappingConfig> exceptionMappingConfigs = new ArrayList<>();

  /**
   * Instantiates rest api exception mapper properties.
   */
  public RestApiExceptionMapperProperties() {

    defaultExceptionMapping = new ExceptionMapping();
    defaultExceptionMapping.setCode(null);
    defaultExceptionMapping.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    defaultExceptionMapping.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    defaultExceptionMapping.setExceptionClassName("*");

    defaultExceptionMappingConfig = new ExceptionMappingConfig();

    exceptionMappings.add(new ExceptionMapping(
        IllegalArgumentException.class.getName(),
        HttpStatus.BAD_REQUEST,
        null));

    exceptionMappings.add(new ExceptionMapping(
        "org.springframework.security.access.AccessDeniedException",
        HttpStatus.FORBIDDEN,
        null));

    exceptionMappings.add(new ExceptionMapping(
        "javax.persistence.EntityNotFoundException",
        HttpStatus.NOT_FOUND,
        null));
  }

  /**
   * Find exception mapping.
   *
   * @param throwable the throwable
   * @return the exception mapping
   */
  @SuppressWarnings("WeakerAccess")
  public ExceptionMapping findExceptionMapping(Throwable throwable) {
    return getExceptionMappings()
        .stream()
        .filter(exceptionMapping -> matches(
            throwable, exceptionMapping.getExceptionClassName()))
        .findFirst()
        .orElse(getDefaultExceptionMapping());
  }

  /**
   * Find exception mapping config.
   *
   * @param throwable the throwable
   * @return the exception mapping config
   */
  @SuppressWarnings("WeakerAccess")
  public ExceptionMappingConfig findExceptionMappingConfig(
      final Throwable throwable) {

    return getExceptionMappingConfigs()
        .stream()
        .filter(exceptionMappingConfig -> matches(
            throwable, exceptionMappingConfig.getExceptionClassName()))
        .findFirst()
        .orElse(getDefaultExceptionMappingConfig());
  }

  private boolean matches(final Throwable throwable, final String exceptionClassName) {
    if (throwable == null || exceptionClassName == null) {
      return false;
    }
    if (throwable.getClass().getName().equals(exceptionClassName)) {
      return true;
    }
    if (exceptionClassName.endsWith(".*")) {
      final String packagePrefix = exceptionClassName.substring(0, exceptionClassName.length() - 1);
      if (throwable.getClass().getName().startsWith(packagePrefix)) {
        return true;
      }
    }
    if (matches(throwable.getCause(), exceptionClassName)) {
      return true;
    }
    return matches(throwable.getClass().getSuperclass(), exceptionClassName);
  }

  private boolean matches(final Class<?> exceptionClass, final String exceptionClassName) {
    if (exceptionClass == null || exceptionClassName == null) {
      return false;
    }
    if (exceptionClass.getName().equals(exceptionClassName)) {
      return true;
    }
    if (exceptionClassName.endsWith(".*")) {
      final String packagePrefix = exceptionClassName.substring(0, exceptionClassName.length() - 1);
      if (exceptionClass.getName().startsWith(packagePrefix)) {
        return true;
      }
    }
    return matches(exceptionClass.getSuperclass(), exceptionClassName);
  }

  /**
   * The exception mapping.
   */
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExceptionMapping {

    /**
     * Instantiates a new exception mapping.
     *
     * @param exceptionClassName the exception class name
     * @param httpStatus         the http status
     * @param code               the code
     */
    @SuppressWarnings("WeakerAccess")
    public ExceptionMapping(String exceptionClassName, HttpStatus httpStatus, String code) {
      this.exceptionClassName = exceptionClassName;
      if (httpStatus != null) {
        this.status = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
      }
      this.code = code;
    }

    @Getter
    @Setter
    private String exceptionClassName;

    @Setter
    private int status;

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String code;

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
      if (HttpStatus.resolve(status) == null) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
      }
      return status;
    }

  }

  /**
   * The exception mapping config.
   */
  @SuppressWarnings("WeakerAccess")
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExceptionMappingConfig {

    @Getter
    @Setter
    private String exceptionClassName;

    @Getter
    @Setter
    private boolean includeExceptionClassName = true;

    @Getter
    @Setter
    private boolean includeApplicationName = true;

    @Getter
    @Setter
    private boolean includePath = true;

    @Getter
    @Setter
    private boolean includeHandler = false;

    @Getter
    @Setter
    private boolean includeStackTrace = false;

    @Getter
    @Setter
    private boolean includeCause = true;

    @Getter
    @Setter
    private boolean evaluateAnnotationFirst = false;

  }

}
