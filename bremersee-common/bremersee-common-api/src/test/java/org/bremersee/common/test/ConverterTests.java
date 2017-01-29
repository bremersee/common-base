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
import org.bremersee.common.converter.ConverterUtils;
import org.bremersee.common.converter.ObjectMapperUtils;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Christian Bremer
 */
public class ConverterTests {

    @Test
    public void testCreateDefaultObjectMapper() {
        System.out.println("Testing creation of default JSON object mapper ...");
        ObjectMapper om = ObjectMapperUtils.createDefaultObjectMapper();
        TestCase.assertNotNull(om);
        System.out.println("Testing creation of default JSON object mapper ... DONE");
    }

    @Test
    public void testDateTomeConverting() {
        System.out.println("Testing date time converting ...");
        final long ct = System.currentTimeMillis();
        final Date date = new Date(ct);
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        TestCase.assertEquals(zonedDateTime, ConverterUtils.mapToZonedDateTime(date));
        TestCase.assertEquals(date, ConverterUtils.mapToDate(zonedDateTime));
        System.out.println("Testing date time converting ... DONE!");
    }

}
