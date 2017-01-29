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

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.bremersee.common.converter.ObjectMapperUtils;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.model.AddressDto;
import org.bremersee.common.model.ThrowableDto;
import org.bremersee.common.model.ThrowableMessageDto;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 * @author Christian Bremer
 */
public class ModelJsonTests {

    private final ObjectMapper om = ObjectMapperUtils.createDefaultObjectMapper();

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

            String jsonStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(a0);

            System.out.println("Address (a0):\n" + jsonStr);

            AddressDto a00 = om.readValue(ModelJsonTests.class.getResourceAsStream("/address_0.json"), AddressDto.class);
            TestCase.assertEquals(a0, a00);

        } catch (IOException e) {
            throw new InternalServerError(e);
        }
    }

    @Test
    public void testThrowableMessageDto() {

        try {
            System.out.println("Testing ThrowableMessageDto ...");
            ThrowableMessageDto leight = new ThrowableMessageDto(new NotFoundException("Your requested entity was not found."));
            String leightStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(leight);
            System.out.println(leightStr);
            ThrowableMessageDto leightRead = om.readValue(leightStr, ThrowableMessageDto.class);
            TestCase.assertEquals(leight, leightRead);
            System.out.println("Testing ThrowableMessageDto ... DONE!");

        } catch (IOException e) {
            throw new InternalServerError(e);
        }
    }

    @Test
    public void testThrowableDto() {

        try {
            System.out.println("Testing ThrowableDto ...");
            ThrowableDto full = new ThrowableDto(new NotFoundException("Nothing found."));
            String fullStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(full);
            System.out.println(fullStr);
            ThrowableDto fullRead = om.readValue(fullStr, ThrowableDto.class);
            TestCase.assertEquals(full, fullRead);
            System.out.println("Testing ThrowableDto ... DONE!");

        } catch (IOException e) {
            throw new InternalServerError(e);
        }
    }

    @Test
    public void testThrowableDtoPolymorphism() {

        try {
            System.out.println("Testing ThrowableDto polymorphism ...");
            ThrowableDto full = new ThrowableDto(new NotFoundException("Nothing found."));
            String fullStr = om.writerWithDefaultPrettyPrinter().writeValueAsString(full);
            System.out.println(fullStr);

            ThrowableMessageDto fullRead = om.readValue(fullStr, ThrowableMessageDto.class);
            System.out.println("Read throwable:");
            System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(fullRead));
            TestCase.assertEquals(full, fullRead);
            System.out.println("Testing ThrowableDto polymorphism ... DONE!");

        } catch (IOException e) {
            throw new InternalServerError(e);
        }
    }

}
