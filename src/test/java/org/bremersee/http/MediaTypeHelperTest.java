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

package org.bremersee.http;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author Christian Bremer
 */
public class MediaTypeHelperTest {

  @Test
  public void testMediaTypeHelper() {
    Assert.assertTrue(MediaTypeHelper.canContentTypeBeJson(MediaTypeHelper.toString(Arrays.asList(
        MediaType.IMAGE_JPEG, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_UTF8))));
    Assert.assertFalse(MediaTypeHelper.canContentTypeBeJson(MediaTypeHelper.toString(Arrays.asList(
        MediaType.IMAGE_JPEG, MediaType.APPLICATION_XML))));
    Assert.assertTrue(MediaTypeHelper.canContentTypeBeXml(MediaTypeHelper.toString(Arrays.asList(
        MediaType.IMAGE_JPEG, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))));
    Assert.assertFalse(MediaTypeHelper.canContentTypeBeXml(MediaTypeHelper.toString(Arrays.asList(
        MediaType.IMAGE_JPEG, MediaType.APPLICATION_JSON_UTF8))));
  }

}
