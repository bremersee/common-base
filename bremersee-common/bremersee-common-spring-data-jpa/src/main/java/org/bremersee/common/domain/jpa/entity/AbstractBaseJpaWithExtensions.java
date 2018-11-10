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

package org.bremersee.common.domain.jpa.entity;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.InternalServerError;

import javax.persistence.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = true, exclude = {"extensions", "extensionsStr"})
@ToString(callSuper = true)
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractBaseJpaWithExtensions extends AbstractBaseJpa {

    private static final long serialVersionUID = 1L;

    private static final JAXBContext jaxbContext;

    private static final ObjectMapper om;

    @SuppressWarnings("ValidExternallyBoundObject")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "extensionsWrapper")
    @XmlType(name = "extensionsWrapperType")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class ExtensionsWrapper {
        private Map<String, Object> extensions = new LinkedHashMap<>();
    }

    static {
        om = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primary, secondary);
        om.setAnnotationIntrospector(pair);
        try {
            jaxbContext = JAXBContext.newInstance(ExtensionsWrapper.class);
        } catch (Exception e) {
            throw new InternalServerError("Creating JAXBContext failed.", e);
        }
    }

    @Basic
    @Lob
    @Column(name = "ext_str", length = 8000000)
    private String extensionsStr;

    @Transient
    private Map<String, Object> extensions = new LinkedHashMap<>(); // NOSONAR

    @PrePersist
    @PreUpdate
    protected void prePersistOrUpdateExtensions() {
        if (extensions == null || extensions.isEmpty()) {
            this.extensionsStr = null;
        } else {
            try {
                om.writeValueAsString(extensions);

            } catch (Exception e) {

                try {
                    StringWriter sw = new StringWriter();
                    jaxbContext.createMarshaller().marshal(new ExtensionsWrapper(extensions), sw);
                    extensionsStr = sw.toString();

                } catch (Exception e0) { // NOSONAR
                    throw new InternalServerError("Creating extension string failed.", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @PostLoad
    protected void postLoadExtensions() {
        if (StringUtils.isNotBlank(extensionsStr)) {
            if (extensionsStr.startsWith("{")) {
                try {
                    extensions = om.readValue(extensionsStr, Map.class);
                } catch (Exception e) {
                    throw new InternalServerError("Reading extension string as JSON failed.", e);
                }
            } else {
                try {
                    extensions = ((ExtensionsWrapper) jaxbContext.createUnmarshaller()
                            .unmarshal(new StringReader(extensionsStr))).getExtensions();
                } catch (Exception e) {
                    throw new InternalServerError("Reading extension string as XML failed.", e);
                }
            }
            extensionsStr = null;
        }
    }

    public Map<String, Object> getExtensions() {
        if (extensions == null) {
            extensions = new LinkedHashMap<>();
        }
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        if (extensions == null) {
            this.extensions = new LinkedHashMap<>();
        } else {
            this.extensions = extensions;
        }
    }

}
