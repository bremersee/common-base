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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.bremersee.common.domain.entity.BaseEntity;
import org.bremersee.common.model.Base;

/**
 * @author Christian Bremer
 *
 */
@MappedSuperclass
public abstract class AbstractBaseJpa<PK extends Serializable> implements BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    protected PK id;
    
    @Column(name = "created", nullable = true)
    protected Long created;
    
    @Column(name = "modified", nullable = true)
    protected Long modified;
    
    @Transient
    private final Map<String, Object> tmpExtensions = new LinkedHashMap<>();
    
    @PrePersist @PreUpdate
    protected void preSave() {
        long ct = System.currentTimeMillis();
        if (created == null) {
            created = ct;
        }
        modified = ct;
    }
    
    /**
     * Default constructor.
     */
    public AbstractBaseJpa() {
    }

    public AbstractBaseJpa(Base obj) {
        if (obj != null) {
            setCreated(obj.getCreated());
            setModified(obj.getModified());
            if (obj.getExtensions() != null) {
                getExtensions().putAll(obj.getExtensions());
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        AbstractBaseJpa other = (AbstractBaseJpa) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Transient
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public PK getId() {
        return id;
    }

    protected void setId(PK id) {
        this.id = id;
    }

    @Override
    public Long getCreated() {
        return created;
    }

    @Override
    public void setCreated(Long created) {
        this.created = created;
    }

    @Override
    public Long getModified() {
        return modified;
    }

    @Override
    public void setModified(Long modified) {
        this.modified = modified;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return tmpExtensions;
    }

}
