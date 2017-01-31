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

package org.bremersee.common.security.acls.domain.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 *
 */
@Entity
@Table(name = "acl_entry")
@Data
@NoArgsConstructor
public class AclEntry implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "acl_object_identity", nullable = false)
    private AclObjectIdentity aclObjectIdentity;
    
    @Column(name = "ace_order", nullable = false)
    private int aceOrder;
    
    @ManyToOne
    @JoinColumn(name = "sid", nullable = false)
    private AclSid sid;
    
    @Column(name = "mask", nullable = false)
    private int mask;
    
    @Column(name = "granting", nullable = false)
    private boolean granting;
    
    @Column(name = "audit_success", nullable = false)
    private boolean auditSuccess;
    
    @Column(name = "audit_failure", nullable = false)
    private boolean auditFailure;

}