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

package org.bremersee.common.converter;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.bremersee.geojson.GeoJsonObjectMapperModule;

/**
 * @author Christian Bremer
 */
public abstract class ObjectMapperUtils { // NOSONAR

    /**
     * Never construct.
     */
    private ObjectMapperUtils() {
        super();
    }

    /**
     * Creates a new {@link ObjectMapper}
     * <ul>
     * <li>
     * with a pair of {@link AnnotationIntrospector}:first is {@link JacksonAnnotationIntrospector} and second
     * is {@link JaxbAnnotationIntrospector},
     * </li>
     * <li>
     * with {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} disabled,
     * </li>
     * <li>
     * with {@link SerializationFeature#WRITE_DATES_WITH_ZONE_ID} disabled
     * </li>
     * <li>
     * and with module {@link GeoJsonObjectMapperModule} registered.
     * </li>
     * </ul>
     *
     * @return the object mapper
     */
    public static ObjectMapper createDefaultObjectMapper() {
        final AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        final AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        final AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primary, secondary);
        final ObjectMapper om = new ObjectMapper();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        om.setAnnotationIntrospector(pair);
        om.registerModule(new GeoJsonObjectMapperModule());
        return om;
    }

}
