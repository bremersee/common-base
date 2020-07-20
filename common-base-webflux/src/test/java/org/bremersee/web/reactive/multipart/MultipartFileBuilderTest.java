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

package org.bremersee.web.reactive.multipart;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * The multipart file builder test.
 *
 * @author Christian Bremer
 */
class MultipartFileBuilderTest {

  /**
   * Gets multipart file from list that does not exist.
   */
  @Test
  void getMultipartFileFromListThatDoesNotExist() {
    assertTrue(MultipartFileBuilder.getMultipartFile(Collections.emptyList(), 1).isEmpty());
  }

  /**
   * Gets multipart file from map that does not exist.
   */
  @Test
  void getMultipartFileFromMapThatDoesNotExist() {
    assertTrue(MultipartFileBuilder.getMultipartFile(Collections.emptyMap(), "foo").isEmpty());
  }

  /**
   * Gets multipart files from map that does not exist.
   */
  @Test
  void getMultipartFilesFromMapThatDoesNotExist() {
    MultiValueMap<String, MultipartFile> map = new LinkedMultiValueMap<>();
    assertTrue(MultipartFileBuilder.getMultipartFiles(map, "foo").isEmpty());
  }

  /**
   * Gets first multipart file from map that does not exist.
   */
  @Test
  void getFirstMultipartFileFromMapThatDoesNotExist() {
    MultiValueMap<String, MultipartFile> map = new LinkedMultiValueMap<>();
    assertTrue(MultipartFileBuilder.getFirstMultipartFile(map, "foo").isEmpty());
  }

}