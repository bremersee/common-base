/*
 * Copyright 2022 the original author or authors.
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

package org.bremersee.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.JAXBException;
import org.bremersee.test.exception.TestFrameworkException;
import org.bremersee.xml.JaxbContextBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;

/**
 * The read write utils.
 *
 * @author Christian Bremer
 */
public class TestReadWriteUtils {

  /**
   * Gets target folder.
   *
   * @return the target folder
   */
  public static Path getTargetFolder() {
    try {
      return Paths
          .get(new ClassPathResource("/").getURI())
          .getParent();
    } catch (Exception e) {
      throw new TestFrameworkException("Could not retrieve target folder.", e);
    }
  }

  /**
   * Write to target folder.
   *
   * @param content the content
   * @param fileName the file name
   * @throws IOException the io exception
   */
  public static void writeToTargetFolder(byte[] content, String fileName) throws IOException {
    try {
      Path filePath = Path.of(getTargetFolder().toString(), fileName);
      Files.write(filePath, content);

    } catch (TestFrameworkException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * Write to target folder.
   *
   * @param content the content
   * @param fileName the file name
   * @throws IOException the io exception
   */
  public static void writeToTargetFolder(String content, String fileName) throws IOException {
    writeToTargetFolder(
        content.getBytes(StandardCharsets.UTF_8),
        fileName);
  }

  public static void writeJsonToTargetFolder(
      String fileName,
      ObjectMapper objectMapper,
      Object model) throws IOException {

    byte[] content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(model);
    writeToTargetFolder(content, fileName);
  }

  public static void writeXmlToTargetFolder(
      String fileName,
      JaxbContextBuilder jaxbContextBuilder,
      Object model) throws IOException, JAXBException {

    StringWriter sw = new StringWriter();
    jaxbContextBuilder.buildMarshaller(model).marshal(model, sw);
    writeToTargetFolder(sw.toString(), fileName);
  }

  /**
   * Read bytes from target folder byte array.
   *
   * @param fileName the file name
   * @return the byte array
   * @throws IOException the io exception
   */
  public static byte[] readBytesFromTargetFolder(String fileName) throws IOException {
    Path filePath = Path.of(getTargetFolder().toString(), fileName);
    return Files.readAllBytes(filePath);
  }

  /**
   * Read string from target folder string.
   *
   * @param fileName the file name
   * @return the string
   * @throws IOException the io exception
   */
  public static String readStringFromTargetFolder(String fileName) throws IOException {
    Path filePath = Path.of(getTargetFolder().toString(), fileName);
    return Files.readString(filePath, StandardCharsets.UTF_8);
  }

  public static <T> T readJsonFromTargetFolder(
      String fileName,
      ObjectMapper objectMapper,
      Class<? extends T> clazz) throws IOException {

    byte[] content = readBytesFromTargetFolder(fileName);
    return objectMapper.readValue(content, clazz);
  }

  public static <T> T readJsonFromTargetFolder(
      String fileName,
      ObjectMapper objectMapper,
      TypeReference<? extends T> typeReference) throws IOException {

    byte[] content = readBytesFromTargetFolder(fileName);
    return objectMapper.readValue(content, typeReference);
  }

  public static <T> T readXmlFromTargetFolder(
      String fileName,
      JaxbContextBuilder jaxbContextBuilder) throws IOException, JAXBException {

    byte[] content = readBytesFromTargetFolder(fileName);
    //noinspection unchecked
    return (T) jaxbContextBuilder.buildUnmarshaller().unmarshal(new ByteArrayInputStream(content));
  }

  /**
   * To class path location. The name of the class is part of the path.
   *
   * @param clazz the clazz
   * @param resourceName the resource name
   * @return the string
   */
  public static String toClassPathLocation(
      Class<?> clazz,
      String resourceName) {
    return ClassUtils.getUserClass(clazz).getName().replaceAll("[.]", "/")
        + "/"
        + resourceName;
  }

  /**
   * To class path location.
   *
   * @param pakkage the pakkage
   * @param resourceName the resource name
   * @return the string
   */
  public static String toClassPathLocation(
      Package pakkage,
      String resourceName) {
    return pakkage.getName().replaceAll("[.]", "/")
        + "/"
        + resourceName;
  }

  /**
   * Read from class path.
   *
   * @param location the location
   * @return the byte array
   * @throws IOException the io exception
   */
  public static byte[] readFromClassPath(String location) throws IOException {
    return FileCopyUtils.copyToByteArray(new ClassPathResource(location).getInputStream());
  }

  /**
   * Read string from class path.
   *
   * @param location the location
   * @return the string
   * @throws IOException the io exception
   */
  public static String readStringFromClassPath(String location) throws IOException {
    return new String(readFromClassPath(location), StandardCharsets.UTF_8);
  }

  public static <T> T readJsonFromClassPath(
      String location,
      ObjectMapper objectMapper,
      Class<? extends T> clazz) throws IOException {

    byte[] content = readFromClassPath(location);
    return objectMapper.readValue(content, clazz);
  }

  public static <T> T readJsonFromClassPath(
      String location,
      ObjectMapper objectMapper,
      TypeReference<? extends T> typeReference) throws IOException {

    byte[] content = readFromClassPath(location);
    return objectMapper.readValue(content, typeReference);
  }

  public static <T> T readXmlFromClassPath(String location, JaxbContextBuilder jaxbContextBuilder)
      throws IOException, JAXBException {

    byte[] content = readFromClassPath(location);
    //noinspection unchecked
    return (T) jaxbContextBuilder.buildUnmarshaller().unmarshal(new ByteArrayInputStream(content));
  }

}
