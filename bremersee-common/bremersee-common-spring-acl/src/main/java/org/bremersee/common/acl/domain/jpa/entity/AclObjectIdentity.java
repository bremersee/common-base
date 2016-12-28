/*
 * Copyright 2015 the original author or authors.
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

package org.bremersee.common.acl.domain.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 *
 */
@Entity
@Table(name = "acl_object_identity", uniqueConstraints = {
        @UniqueConstraint(name = "acl_object_identity_uc_class_identity",
                columnNames = { "object_id_class", "object_id_identity" })
})
@Data
@NoArgsConstructor
public class AclObjectIdentity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "object_id_class", nullable = false)
    private AclClass objectIdClass;
    
    @Column(name = "object_id_identity", nullable = false)
    private Long objectIdIdentity;
    
    @ManyToOne
    @JoinColumn(name = "parent_object")
    private AclObjectIdentity parentObject;
    
    @ManyToOne
    @JoinColumn(name = "owner_sid")
    private AclSid ownerSid;
    
    @Column(name = "entries_inheriting", nullable = false)
    private boolean entriesInheriting;

}
