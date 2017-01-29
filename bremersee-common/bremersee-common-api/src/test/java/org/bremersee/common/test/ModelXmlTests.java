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

package org.bremersee.common.test;

import junit.framework.TestCase;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.common.model.AddressDto;
import org.bremersee.common.model.ObjectFactory;
import org.bremersee.utils.CodingUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author Christian Bremer
 */
public class ModelXmlTests {

    private JAXBContext jaxbContext;

    @Before
    public void createJAXBContext() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
    }

    @Test
    public void testAddressDto() {
        try {
            AddressDto a0 = new AddressDto();
            a0.setCity("Hamburg");
            a0.setCountry("Germany");
            a0.setCreated(new Date(1468933888608L));
            a0.setId("111");
            a0.setStreetAddress("Reeperbahn");
            a0.setStreetNumber("26a");
            //a0.addExtension("lat", 52.7);
            //a0.addExtension("lng", 10.853);

            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            m.marshal(a0, bout);
            byte[] bytes = bout.toByteArray();

            String xmlStr = CodingUtils.toStringSilently(bytes, StandardCharsets.UTF_8);

            System.out.println("Address (a0):\n" + xmlStr);

            AddressDto a00 = (AddressDto) jaxbContext.createUnmarshaller().unmarshal(ModelXmlTests.class.getResourceAsStream("/address_0.xml"));
            TestCase.assertEquals(a0, a00);

            System.out.println("Lombok's toString(): \n" + a00.toString());

        } catch (JAXBException e) {
            throw new InternalServerError(e);
        }
    }

}
