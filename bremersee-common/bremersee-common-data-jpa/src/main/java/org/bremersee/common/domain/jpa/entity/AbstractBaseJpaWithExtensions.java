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

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.model.Base;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christian Bremer
 *
 */
@MappedSuperclass
public abstract class AbstractBaseJpaWithExtensions<PK extends Serializable> extends AbstractBaseJpa<PK> {
    
    private static final long serialVersionUID = 1L;
    
    private static final JAXBContext jaxbContext;
    
    private static final ObjectMapper om;
    
    @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
    @XmlRootElement(name = "extensionsWrapper")
    @XmlType(name = "extensionsWrapperType")
    protected static class ExtensionsWrapper {
        
        public Map<String, Object> extensions = new LinkedHashMap<>();
        
        public ExtensionsWrapper() {
        }
        
        public ExtensionsWrapper(Map<String, Object> extensions) {
            if (extensions != null) {
                this.extensions = extensions;
            }
        }
    }
    
    static {
        om = new ObjectMapper();
        try {
            jaxbContext = JAXBContext.newInstance(ExtensionsWrapper.class);
        } catch (Exception e) {
            throw new RuntimeException("Creating JAXBContext failed.", e);
        }
    }
    
    @Basic(optional = true)
    @Lob
    @Column(name = "ext_str", length = 8000000, nullable = true)
    private String extensionsStr;
    
    @Transient
    private Map<String, Object> extensions = new LinkedHashMap<String, Object>();
    
    @PrePersist @PreUpdate
    protected void preSave() {
        super.preSave();
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
                    
                } catch (Exception e0) {
                    throw new RuntimeException("Creating extension string failed.", e);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @PostLoad
    protected void postLoad() {
        if (StringUtils.isNotBlank(extensionsStr)) {
            if (extensionsStr.startsWith("{")) {
                try {
                    extensions = om.readValue(extensionsStr, Map.class);
                } catch (Exception e) {
                    throw new RuntimeException("Reading extension string as JSON failed.", e);
                }
            } else {
                try {
                    extensions = ((ExtensionsWrapper) jaxbContext.createUnmarshaller().unmarshal(new StringReader(extensionsStr))).extensions;
                } catch (Exception e) {
                    throw new RuntimeException("Reading extension string as XML failed.", e);
                }
            }
        }
    }

    /**
     * Default constructor.
     */
    public AbstractBaseJpaWithExtensions() {
    }

    public AbstractBaseJpaWithExtensions(Base obj) {
        if (obj != null) {
            setCreated(obj.getCreated());
            setModified(obj.getModified());
            if (obj.getExtensions() != null) {
                getExtensions().putAll(obj.getExtensions());
            }
        }
    }

    @Override
    public Map<String, Object> getExtensions() {
        if (extensions == null) {
            extensions = new LinkedHashMap<>();
        }
        return extensions;
    }

    @Override
    public void setExtensions(Map<String, Object> extensions) {
        if (extensions == null) {
            extensions = new LinkedHashMap<>();
        }
        this.extensions = extensions;
    }

}
