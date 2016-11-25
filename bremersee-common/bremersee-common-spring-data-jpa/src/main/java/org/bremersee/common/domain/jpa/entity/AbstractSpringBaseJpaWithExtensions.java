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

import org.bremersee.common.model.Base;

/**
 * @author Christian Bremer
 *
 */
@MappedSuperclass
public abstract class AbstractSpringBaseJpaWithExtensions<PK extends Serializable> extends AbstractBaseJpaWithExtensions<PK> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor.
     */
    public AbstractSpringBaseJpaWithExtensions() {
    }

    public AbstractSpringBaseJpaWithExtensions(Base obj) {
        super(obj);
    }

}
