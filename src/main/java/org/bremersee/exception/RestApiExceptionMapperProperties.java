/*
 * Copyright 2017 the original author or authors.
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
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.exception-mapping")
@Data
public class RestApiExceptionMapperProperties {

  private List<String> apiPaths = new ArrayList<>();

  private ExceptionMapping defaultExceptionMapping;

  private List<ExceptionMapping> exceptionMappings = new ArrayList<>();

  private ExceptionMappingConfig defaultExceptionMappingConfig;

  private List<ExceptionMappingConfig> exceptionMappingConfigs = new ArrayList<>();

  public RestApiExceptionMapperProperties() {

    apiPaths.add("/api/**");

    defaultExceptionMapping = new ExceptionMapping();
    defaultExceptionMapping.setCode(ExceptionConstants.NO_ERROR_CODE_PRESENT);
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

  public ExceptionMapping findExceptionMapping(Throwable throwable) {
    return getExceptionMappings()
        .stream()
        .filter(exceptionMapping -> matches(
            throwable, exceptionMapping.getExceptionClassName()))
        .findFirst()
        .orElse(getDefaultExceptionMapping());
  }

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

  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExceptionMapping {

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

    public int getStatus() {
      if (HttpStatus.resolve(status) == null) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
      }
      return status;
    }

  }

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
    private boolean includeRequestPath = true;

    @Getter
    @Setter
    private boolean includeHandler = true;

    @Getter
    @Setter
    private boolean includeStackTrace = true;

    @Getter
    @Setter
    private boolean includeCause = true;

    @Getter
    @Setter
    private boolean evaluateAnnotationFirst = false;

  }

}
