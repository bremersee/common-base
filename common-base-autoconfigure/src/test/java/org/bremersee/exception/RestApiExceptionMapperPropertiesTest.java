package org.bremersee.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMapping;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * The rest api exception mapper properties test.
 *
 * @author Christian Bremer
 */
class RestApiExceptionMapperPropertiesTest {

  /**
   * Tests find exception mapping.
   */
  @Test
  void findExceptionMapping() {
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    ExceptionMapping mapping = properties.findExceptionMapping(new Exception());
    assertEquals(properties.getDefaultExceptionMapping(), mapping);

    mapping = properties.findExceptionMapping(new IllegalArgumentException());
    assertEquals(HttpStatus.BAD_REQUEST.value(), mapping.getStatus());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), mapping.getMessage());

    ExceptionMapping newMapping = new ExceptionMapping();
    newMapping.setExceptionClassName(IllegalAccessException.class.getName());
    newMapping.setCode("ILLEGAL_ACCESS");
    newMapping.setMessage("Illegal Access");
    newMapping.setStatus(500);
    properties.getExceptionMappings().add(newMapping);

    mapping = properties.findExceptionMapping(new IllegalAccessException());
    assertEquals(500, mapping.getStatus());
    assertEquals("Illegal Access", mapping.getMessage());
    assertEquals("ILLEGAL_ACCESS", mapping.getCode());
  }

  /**
   * Tests find exception mapping config.
   */
  @Test
  void findExceptionMappingConfig() {
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    ExceptionMappingConfig config = properties.findExceptionMappingConfig(new Exception());
    assertEquals(properties.getDefaultExceptionMappingConfig(), config);

    ExceptionMappingConfig newConfig = new ExceptionMappingConfig();
    newConfig.setEvaluateAnnotationFirst(true);
    newConfig.setExceptionClassName(IllegalAccessException.class.getName());
    newConfig.setIncludeApplicationName(false);
    newConfig.setIncludeCause(false);
    newConfig.setIncludeExceptionClassName(false);
    newConfig.setIncludeHandler(true);
    newConfig.setIncludePath(false);
    newConfig.setIncludeStackTrace(true);

    properties.getExceptionMappingConfigs().add(newConfig);
    config = properties.findExceptionMappingConfig(new IllegalAccessException());
    assertEquals(newConfig, config);

    assertNotNull(properties.toString());
    assertEquals(properties, properties);

    ExceptionMappingConfig moreConfig = new ExceptionMappingConfig();
    moreConfig.setEvaluateAnnotationFirst(true);
    moreConfig.setExceptionClassName(IllegalAccessException.class.getName());
    moreConfig.setIncludeApplicationName(false);
    moreConfig.setIncludeCause(false);
    moreConfig.setIncludeExceptionClassName(false);
    moreConfig.setIncludeHandler(true);
    moreConfig.setIncludePath(false);
    moreConfig.setIncludeStackTrace(true);

    RestApiExceptionMapperProperties moreProperties = new RestApiExceptionMapperProperties();
    moreProperties.getExceptionMappingConfigs().add(moreConfig);
    assertEquals(properties, moreProperties);
  }

  /**
   * Tests get api paths.
   */
  @Test
  void getApiPaths() {
    List<String> paths = new ArrayList<>();
    paths.add("/foo/**");
    paths.add("/bar/**");
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    properties.setApiPaths(paths);
    assertEquals(paths, properties.getApiPaths());
  }

  /**
   * Tests get default exception mapping.
   */
  @Test
  void getDefaultExceptionMapping() {
    ExceptionMapping newMapping = new ExceptionMapping();
    newMapping.setExceptionClassName(IllegalAccessException.class.getName());
    newMapping.setCode("ILLEGAL_ACCESS");
    newMapping.setMessage("Illegal Access");
    newMapping.setStatus(500);
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    properties.setDefaultExceptionMapping(newMapping);
    assertEquals(newMapping, properties.getDefaultExceptionMapping());
  }

  /**
   * Tests get exception mappings.
   */
  @Test
  void getExceptionMappings() {
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    List<ExceptionMapping> mappings = properties.getExceptionMappings();
    assertTrue(
        mappings
            .stream()
            .anyMatch(exceptionMapping -> IllegalArgumentException.class.getName()
                .equals(exceptionMapping.getExceptionClassName())));
    assertTrue(
        mappings
            .stream()
            .anyMatch(
                exceptionMapping -> "org.springframework.security.access.AccessDeniedException"
                    .equals(exceptionMapping.getExceptionClassName())));
    assertTrue(
        mappings
            .stream()
            .anyMatch(
                exceptionMapping -> "javax.persistence.EntityNotFoundException"
                    .equals(exceptionMapping.getExceptionClassName())));

    ExceptionMapping newMapping = new ExceptionMapping();
    newMapping.setExceptionClassName(IllegalAccessException.class.getName());
    newMapping.setCode("ILLEGAL_ACCESS");
    newMapping.setMessage("Illegal Access");
    newMapping.setStatus(500);
    mappings.add(newMapping);
    properties.setExceptionMappings(mappings);
    assertTrue(properties.getExceptionMappings().contains(newMapping));
  }

  /**
   * Tests get default exception mapping config.
   */
  @Test
  void getDefaultExceptionMappingConfig() {
    ExceptionMappingConfig newConfig = new ExceptionMappingConfig();
    newConfig.setEvaluateAnnotationFirst(true);
    newConfig.setExceptionClassName(IllegalAccessException.class.getName());
    newConfig.setIncludeApplicationName(false);
    newConfig.setIncludeCause(false);
    newConfig.setIncludeExceptionClassName(false);
    newConfig.setIncludeHandler(true);
    newConfig.setIncludePath(false);
    newConfig.setIncludeStackTrace(true);
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    properties.setDefaultExceptionMappingConfig(newConfig);
    assertEquals(newConfig, properties.getDefaultExceptionMappingConfig());
  }

  /**
   * Tests get exception mapping configs.
   */
  @Test
  void getExceptionMappingConfigs() {
    RestApiExceptionMapperProperties properties = new RestApiExceptionMapperProperties();
    List<ExceptionMappingConfig> configs = properties.getExceptionMappingConfigs();
    assertNotNull(configs);
    assertTrue(configs.isEmpty());

    ExceptionMappingConfig newConfig = new ExceptionMappingConfig();
    newConfig.setEvaluateAnnotationFirst(true);
    newConfig.setExceptionClassName(IllegalAccessException.class.getName());
    newConfig.setIncludeApplicationName(false);
    newConfig.setIncludeCause(false);
    newConfig.setIncludeExceptionClassName(false);
    newConfig.setIncludeHandler(true);
    newConfig.setIncludePath(false);
    newConfig.setIncludeStackTrace(true);
    configs.add(newConfig);
    properties.setExceptionMappingConfigs(configs);
    assertTrue(properties.getExceptionMappingConfigs().contains(newConfig));
  }

}