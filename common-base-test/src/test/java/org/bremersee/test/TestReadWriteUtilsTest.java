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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * The test read write utils test.
 */
class TestReadWriteUtilsTest {

  /**
   * Gets target folder.
   */
  @Test
  void getTargetFolder() {
    Path actual = TestReadWriteUtils.getTargetFolder();
    assertThat(actual)
        .isNotNull()
        .extracting(path -> path.getName(path.getNameCount() - 1))
        .extracting(Path::toString)
        .isEqualTo("target");
  }

  /**
   * Write and read bytes to target folder.
   *
   * @throws IOException the io exception
   */
  @Test
  void writeAndReadBytesToTargetFolder() throws IOException {
    byte[] expected = "Hello world".getBytes(StandardCharsets.UTF_8);
    TestReadWriteUtils.writeToTargetFolder(expected, "test_bytes.txt");
    byte[] actual = TestReadWriteUtils.readBytesFromTargetFolder("test_bytes.txt");
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * Write and read string to target folder.
   *
   * @throws IOException the io exception
   */
  @Test
  void writeAndReadStringToTargetFolder() throws IOException {
    String expected = "Hello world";
    TestReadWriteUtils.writeToTargetFolder(expected, "test_string.txt");
    String actual = TestReadWriteUtils.readStringFromTargetFolder("test_string.txt");
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * To class path location with class.
   */
  @Test
  void toClassPathLocationWithClass() {
    String actual = TestReadWriteUtils.toClassPathLocation(
        TestReadWriteUtilsTest.class,
        "read_write_test.txt");
    String expected = "org/bremersee/test/TestReadWriteUtilsTest/read_write_test.txt";
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * To class path location with package.
   */
  @Test
  void toClassPathLocationWithPackage() {
    String actual = TestReadWriteUtils.toClassPathLocation(
        TestReadWriteUtilsTest.class.getPackage(),
        "read_write_test.txt");
    String expected = "org/bremersee/test/read_write_test.txt";
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * Read from class path.
   *
   * @throws IOException the io exception
   */
  @Test
  void readFromClassPath() throws IOException {
    byte[] expected = "Hello world".getBytes(StandardCharsets.UTF_8);
    byte[] actual = TestReadWriteUtils.readFromClassPath("org/bremersee/test/read_write_test.txt");
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * Read string from class path.
   *
   * @throws IOException the io exception
   */
  @Test
  void readStringFromClassPath() throws IOException {
    String expected = "Hello world";
    String actual = TestReadWriteUtils
        .readStringFromClassPath("org/bremersee/test/read_write_test.txt");
    assertThat(actual).isEqualTo(expected);
  }
}