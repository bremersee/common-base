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

package org.bremersee.common.domain.mongodb.entity;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Auditable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigInteger;
import java.util.Date;

/**
 * @author Christian Bremer
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "version"})
@ToString
@NoArgsConstructor
public abstract class AbstractBaseMongo implements Persistable<BigInteger> {

    private static final long serialVersionUID = 1L;

    @Id
    private BigInteger id;

    @Version
    private Long version;

    @CreatedDate
    private Date created;

    @CreatedBy
    @Indexed(sparse = true)
    private String createdBy;

    @LastModifiedDate
    private Date modified;

    @LastModifiedBy
    private String modifiedBy;

    @Override
    public boolean isNew() {
        return getId() == null;
    }

}
