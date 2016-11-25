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

package org.bremersee.common.acl.domain.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author Christian Bremer
 *
 */
@Entity
@Table(name = "acl_object_identity", uniqueConstraints = {
        @UniqueConstraint(name = "unique_uk_3", columnNames = { "object_id_class", "object_id_identity" })
})
public class AclObjectIdentity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "object_id_class", nullable = false)
    private AclClass ObjectIdClass;
    
    @Column(name = "object_id_identity", nullable = false)
    private Long objectIdIdentity;
    
    @ManyToOne
    @JoinColumn(name = "parent_object", nullable = true)
    private AclObjectIdentity parentObject;
    
    @ManyToOne
    @JoinColumn(name = "owner_sid", nullable = true)
    private AclSid ownerSid;
    
    @Column(name = "entries_inheriting", nullable = false)
    private boolean entriesInheriting;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AclClass getObjectIdClass() {
        return ObjectIdClass;
    }

    public void setObjectIdClass(AclClass objectIdClass) {
        ObjectIdClass = objectIdClass;
    }

    public Long getObjectIdIdentity() {
        return objectIdIdentity;
    }

    public void setObjectIdIdentity(Long objectIdIdentity) {
        this.objectIdIdentity = objectIdIdentity;
    }

    public AclObjectIdentity getParentObject() {
        return parentObject;
    }

    public void setParentObject(AclObjectIdentity parentObject) {
        this.parentObject = parentObject;
    }

    public AclSid getOwnerSid() {
        return ownerSid;
    }

    public void setOwnerSid(AclSid ownerSid) {
        this.ownerSid = ownerSid;
    }

    public boolean isEntriesInheriting() {
        return entriesInheriting;
    }

    public void setEntriesInheriting(boolean entriesInheriting) {
        this.entriesInheriting = entriesInheriting;
    }

}
