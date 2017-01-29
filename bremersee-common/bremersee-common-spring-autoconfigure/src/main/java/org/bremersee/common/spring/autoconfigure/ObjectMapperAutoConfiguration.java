/*
 * Copyright 2016 the original author or authors.
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

package org.bremersee.common.spring.autoconfigure;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.bremersee.common.converter.ObjectMapperAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Christian Bremer
 */
@Configuration
public class ObjectMapperAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectMapperAutoConfiguration.class);

    private static final String JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME = "com.fasterxml.jackson.module.jaxb" +
            ".JaxbAnnotationIntrospector";

    private static final String GEO_JSON_MAPPER_MODULE_CLASS_NAME = "org.bremersee.geojson.GeoJsonObjectMapperModule";

    private List<ObjectMapperAware> objectMapperAwareBeans = new ArrayList<>();

    private ObjectMapper objectMapper;

    @Autowired(required = false)
    public void setObjectMapperAwareBeans(List<ObjectMapperAware> objectMapperAwareBeans) {
        if (objectMapperAwareBeans != null) {
            this.objectMapperAwareBeans = objectMapperAwareBeans;
        }
    }

    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // @formatter:off
        LOG.info("\n"
                + "**********************************************************************\n" // NOSONAR
                + "*  Jackson2 ObjectMapper Aware Auto Configuration                    *\n"
                + "**********************************************************************\n" // NOSONAR
                + "* - objectMapperAwareBeans: size = " + objectMapperAwareBeans.size() + "\n"
                + "* - objectMapper: " + objectMapper + "\n"
                + "**********************************************************************"); // NOSONAR
        // @formatter:on

        if (objectMapper != null) {
            for (ObjectMapperAware bean : objectMapperAwareBeans) {
                bean.setObjectMapper(objectMapper);
            }
        }
    }

    private static AnnotationIntrospector findAnnotationIntrospectorPair() {
        try {
            LOG.info("Trying to add a pair of annotation introspectors ('"
                    + JacksonAnnotationIntrospector.class.getSimpleName() + " + "
                    + JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME + ").");
            @SuppressWarnings("unchecked")
            Class<? extends AnnotationIntrospector> cls = (Class<? extends AnnotationIntrospector>) Class
                    .forName(JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME);
            Constructor<? extends AnnotationIntrospector> constructor = cls.getConstructor(TypeFactory.class);
            AnnotationIntrospector secondary = constructor.newInstance(TypeFactory.defaultInstance());

            // see http://wiki.fasterxml.com/JacksonJAXBAnnotations
            AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
            AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primary, secondary);
            LOG.info("The pair of annotation introspectors ('" // NOSONAR
                    + JacksonAnnotationIntrospector.class.getSimpleName()
                    + " + " + JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME + ") was successfully added.");
            return pair;

        } catch (ClassNotFoundException e) { // NOSONAR
            LOG.warn("The pair of annotation introspectors ('"
                    + JacksonAnnotationIntrospector.class.getSimpleName() + " " +
                    "+ " + JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME + ") wasn't added: "
                    + JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME + " was not found.");
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("The pair of annotation introspectors ('"
                    + JacksonAnnotationIntrospector.class.getSimpleName() + " " +
                    "+ " + JAXB_ANNOTATION_INTROSPECTOR_CLASS_NAME + ") wasn't added.", e);
        }
        return null;

    }

    private static boolean addObjectMapperModule(final List<Module> moduleList, final String className) {
        try {
            LOG.info("Trying to add module'" + className + "' to the Jackson Object Mapper.");
            @SuppressWarnings("unchecked")
            Class<? extends Module> cls = (Class<? extends Module>) Class
                    .forName(className);
            moduleList.add(cls.newInstance());
            LOG.info("Module'" + className + "' was successfully added to the Jackson Object Mapper."); // NOSONAR
            return true;

        } catch (ClassNotFoundException e) { // NOSONAR
            LOG.warn("Module'" + className + "' wasn't added to the Jackson Object Mapper: "
                    + className + " was not found.");
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.warn("Module'" + className + "' wasn't added to the Jackson Object Mapper.", e);
        }
        return false;
    }

    private static void logInfo(String annotationIntrospectorsInfo, String geoJsonModuleInfo) {
        // @formatter:off
        LOG.info("\n"
                + "**********************************************************************\n"
                + "*  Jackson2 ObjectMapper Auto Configuration                          *\n"
                + "**********************************************************************\n"
                + "* - Try to add 'JacksonAnnotationIntrospector' as primary            *\n"
                + "*   and 'JaxbAnnotationIntrospector' as secondary                    *\n"
                + "*   annotation introspector: " + annotationIntrospectorsInfo
                + "* - Try to add 'GeoJsonObjectMapperModule' to the mapper: " + geoJsonModuleInfo
                + "**********************************************************************");
        // @formatter:on
    }

    @Configuration
    @ConditionalOnNotWebApplication
    static class NotWebApplicationAutoConfiguration {

        private JacksonProperties properties = new JacksonProperties();

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Autowired(required = false)
        public void setProperties(JacksonProperties properties) {
            if (properties != null) {
                this.properties = properties;
            }
        }

        @PostConstruct
        public void init() {
            applyProperties();

            AnnotationIntrospector annotationIntrospectorPair = findAnnotationIntrospectorPair();
            final String annotationIntrospectorsInfo;
            if (annotationIntrospectorPair != null) {
                objectMapper.setAnnotationIntrospector(annotationIntrospectorPair);
                annotationIntrospectorsInfo = "OK                                      *\n";
            } else {
                annotationIntrospectorsInfo = "FAILED                                  *\n";
            }

            final String geoJsonModuleInfo;
            List<Module> moduleList = new LinkedList<>();

            if (addObjectMapperModule(moduleList, GEO_JSON_MAPPER_MODULE_CLASS_NAME)) {
                geoJsonModuleInfo = "OK         *\n";
            } else {
                geoJsonModuleInfo = "FAILED     *\n";
            }
            if (!moduleList.isEmpty()) {
                objectMapper.registerModules(moduleList);
            }

            logInfo(annotationIntrospectorsInfo, geoJsonModuleInfo);
        }

        @Bean
        @Primary
        public ObjectMapper objectMapper() {
            return objectMapper;
        }

        private void applyProperties() {

            // http://wiki.fasterxml.com/JacksonFAQDateHandling
            // http://docs.spring.io/spring-boot/docs/current/reference/html/howto-spring-mvc.html#howto-customize-the-jackson-objectmapper
            if (properties.getSerialization().get(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) == null) {
                properties.getSerialization().put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, Boolean.FALSE);
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
            if (Boolean.FALSE.equals(properties.getSerialization().get(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                    && properties.getSerialization().get(SerializationFeature.WRITE_DATES_WITH_ZONE_ID) == null) {
                properties.getSerialization().put(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, Boolean.FALSE);
                objectMapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
            }
        }
    }

    @Configuration
    @ConditionalOnClass(name = {"org.springframework.http.converter.json.Jackson2ObjectMapperBuilder"})
    @ConditionalOnWebApplication
    static class WebApplicationAutoConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

        private JacksonProperties properties = new JacksonProperties();

        @Autowired(required = false)
        public void setProperties(JacksonProperties properties) {
            if (properties != null) {
                this.properties = properties;
            }
        }

        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {

            applyProperties(jacksonObjectMapperBuilder);

            AnnotationIntrospector annotationIntrospectorPair = findAnnotationIntrospectorPair();
            final String annotationIntrospectorsInfo;
            if (annotationIntrospectorPair != null) {
                jacksonObjectMapperBuilder.annotationIntrospector(annotationIntrospectorPair);
                annotationIntrospectorsInfo = "OK                                      *\n";
            } else {
                annotationIntrospectorsInfo = "FAILED                                  *\n";
            }

            final String geoJsonModuleInfo;
            List<Module> moduleList = new LinkedList<>();

            if (addObjectMapperModule(moduleList, GEO_JSON_MAPPER_MODULE_CLASS_NAME)) {
                geoJsonModuleInfo = "OK         *\n";
            } else {
                geoJsonModuleInfo = "FAILED     *\n";
            }
            if (!moduleList.isEmpty()) {
                jacksonObjectMapperBuilder.modules(moduleList);
            }

            logInfo(annotationIntrospectorsInfo, geoJsonModuleInfo);
        }

        private void applyProperties(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            // http://wiki.fasterxml.com/JacksonFAQDateHandling
            // http://docs.spring.io/spring-boot/docs/current/reference/html/howto-spring-mvc.html#howto-customize-the-jackson-objectmapper
            if (properties.getSerialization().get(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) == null) {
                properties.getSerialization().put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, Boolean.FALSE);
                jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
            if (Boolean.FALSE.equals(properties.getSerialization().get(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                    && properties.getSerialization().get(SerializationFeature.WRITE_DATES_WITH_ZONE_ID) == null) {
                properties.getSerialization().put(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, Boolean.FALSE);
                jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
            }
        }

    }

}
