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

import javax.persistence.MappedSuperclass;

import org.bremersee.common.domain.entity.BaseEntity;
import org.bremersee.common.model.Base;
import org.springframework.data.domain.Persistable;

/**
 * @author Christian Bremer
 *
 */
@MappedSuperclass
public abstract class AbstractSpringBaseJpa<PK extends Serializable> extends AbstractBaseJpa<PK> implements BaseEntity, Persistable<PK> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor.
     */
    public AbstractSpringBaseJpa() {
    }

    public AbstractSpringBaseJpa(Base obj) {
        super(obj);
    }

}
