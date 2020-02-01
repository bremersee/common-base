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

package org.bremersee.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * The model mapper auto configuration test.
 *
 * @author Christian Bremer
 */
class ModelMapperAutoConfigurationTest {

  /**
   * Tests creation of model mapper.
   */
  @Test
  void modelMapper() {
    ModelMapperAutoConfiguration configuration = new ModelMapperAutoConfiguration();
    configuration.init();
    assertNotNull(configuration.modelMapper());
  }

}