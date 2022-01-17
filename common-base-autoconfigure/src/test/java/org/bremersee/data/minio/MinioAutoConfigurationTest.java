/*
 * Copyright 2020-2022 the original author or authors.
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

package org.bremersee.data.minio;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.minio.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * The minio autoconfiguration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "spring.application.name=minio-test",
        "bremersee.minio.url=https://play.min.io",
        "bremersee.minio.access-key=Q3AM3UQ867SPQQA43P2F",
        "bremersee.minio.secret-key=zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG",
    })
@Slf4j
public class MinioAutoConfigurationTest {

  @Autowired(required = false)
  private MinioOperations minioOperations;

  /**
   * Minio operations.
   */
  @Test
  void minioOperations() {
    assertNotNull(minioOperations);
  }

}
