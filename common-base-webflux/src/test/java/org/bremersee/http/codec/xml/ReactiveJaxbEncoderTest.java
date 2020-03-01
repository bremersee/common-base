/*
 * Copyright 2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ServiceLoader;
import org.bremersee.http.codec.xml.model.XmlTestJaxbContextDataProvider;
import org.bremersee.http.codec.xml.model.xml1.Person;
import org.bremersee.http.codec.xml.model.xml2.Vehicle;
import org.bremersee.http.codec.xml.model.xml3.Company;
import org.bremersee.http.codec.xml.model.xml4.Address;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.util.MimeTypeUtils;

/**
 * The reactive jaxb encoder test.
 *
 * @author Christian Bremer
 */
class ReactiveJaxbEncoderTest {

  /**
   * Test can encode.
   */
  @Test
  void testCanEncode() {
    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .withCanMarshal(JaxbContextBuilder.CAN_MARSHAL_ONLY_PREDEFINED_DATA)
        .withCanUnmarshal(JaxbContextBuilder.CAN_UNMARSHAL_ONLY_PREDEFINED_DATA)
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    ReactiveJaxbEncoder encoder = new ReactiveJaxbEncoder(jaxbContextBuilder);

    assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Vehicle.class), MimeTypeUtils.APPLICATION_XML));

    assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Company.class), MimeTypeUtils.APPLICATION_XML));

    assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Address.class), MimeTypeUtils.APPLICATION_XML));

    assertFalse(
        encoder
            .canEncode(ResolvableType.forRawClass(Vehicle.class), MimeTypeUtils.APPLICATION_JSON));

    assertFalse(
        encoder
            .canEncode(
                ResolvableType.forRawClass(XmlTestJaxbContextDataProvider.class),
                MimeTypeUtils.APPLICATION_XML));

    encoder = new ReactiveJaxbEncoder(jaxbContextBuilder);

    assertFalse(
        encoder
            .canEncode(ResolvableType.forRawClass(Person.class), MimeTypeUtils.APPLICATION_XML));

    assertTrue(
        encoder
            .canEncode(ResolvableType.forRawClass(Vehicle.class), MimeTypeUtils.APPLICATION_XML));
  }

}
