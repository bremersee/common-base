/*
 * Copyright 2015 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.bremersee.common.model.AddressDto;
import org.bremersee.common.model.ObjectFactory;
import org.bremersee.utils.CodingUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Bremer
 *
 */
public class XmlSchemaTests {

    private JAXBContext jaxbContext;

    @Before
    public void createJAXBContext() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
    }

    @Test
    public void testXmlSchema() throws Exception {

        System.out.println("Testing XML schema ...");

        BufferSchemaOutputResolver res = new BufferSchemaOutputResolver();
        jaxbContext.generateSchema(res);
        System.out.print(res);

        System.out.println("OK\n");
    }

    @Test
    public void testAddressDto() {
        try {
            AddressDto a0 = new AddressDto();
            a0.setCity("Hamburg");
            a0.setCountry("Germany");
            a0.setCreated(1468933888608L);
            a0.setDbId("111");
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
            
            AddressDto a00 = (AddressDto)jaxbContext.createUnmarshaller().unmarshal(XmlSchemaTests.class.getResourceAsStream("/address_0.xml"));
            Assert.assertEquals(a0, a00);
            
            System.out.println("Lombok's toString(): \n" + a00.toString());
            
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw new RuntimeException(e);
        }
    }

}
