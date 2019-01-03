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

package org.bremersee.http.codec.xml;

import java.util.ServiceLoader;
import org.bremersee.model.XmlTestJaxbContextDataProvider;
import org.bremersee.model.xml1.Person;
import org.bremersee.model.xml2.Vehicle;
import org.bremersee.model.xml3.Company;
import org.bremersee.model.xml4.Address;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.ResolvableType;
import org.springframework.util.MimeTypeUtils;

/**
 * The reactive jaxb encoder test.
 *
 * @author Christian Bremer
 */
public class ReactiveJaxbEncoderTest {

  /**
   * Test can encode.
   */
  @Test
  public void testCanEncode() {
    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    ReactiveJaxbEncoder encoder = new ReactiveJaxbEncoder(jaxbContextBuilder);

    Assert.assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Person.class), null));

    Assert.assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Person.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Vehicle.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Company.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Address.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertFalse(
        encoder
            .canEncode(ResolvableType.forRawClass(Vehicle.class), MimeTypeUtils.APPLICATION_JSON));

    Assert.assertFalse(
        encoder
            .canEncode(
                ResolvableType.forRawClass(XmlTestJaxbContextDataProvider.class),
                MimeTypeUtils.APPLICATION_XML));

    encoder = new ReactiveJaxbEncoder(
        jaxbContextBuilder,
        "http://bremersee.org/xmlschemas/common-xml-test-model-2");

    Assert.assertFalse(
        encoder
            .canEncode(ResolvableType.forRawClass(Person.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Vehicle.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertFalse(
        encoder
            .canEncode(ResolvableType.forRawClass(Company.class), MimeTypeUtils.APPLICATION_XML));

    Assert.assertFalse(
        encoder
            .canEncode(ResolvableType.forRawClass(Address.class), MimeTypeUtils.APPLICATION_XML));
  }

}
