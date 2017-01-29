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

import org.bremersee.common.exception.InternalServerError;
import org.bremersee.common.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author Christian Bremer
 */
public class ModelXmlSchemaTests {

    private JAXBContext jaxbContext;

    @Before
    public void createJAXBContext() {
        try {
            this.jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());

        } catch (JAXBException e) {
            throw new InternalServerError(e);
        }
    }

    @Test
    public void testXmlSchema() {
        try {
            System.out.println("Testing XML schema ...");

            BufferSchemaOutputResolver res = new BufferSchemaOutputResolver();
            jaxbContext.generateSchema(res);
            System.out.print(res);

            System.out.println("OK\n");

        } catch (IOException e) {
            throw new InternalServerError(e);
        }
    }

}
