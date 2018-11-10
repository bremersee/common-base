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

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 */
@Entity
@Table(name = "acl_sid", uniqueConstraints = {
        @UniqueConstraint(name = "acl_sid_uc_sid_principal", columnNames = {"sid", "principal"})
})
@Getter
@Setter
@EqualsAndHashCode(of = {"principal", "sid"})
@ToString
@NoArgsConstructor
public class AclSid implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "principal", nullable = false)
    private boolean principal;

    @Column(name = "sid", nullable = false)
    private String sid;

}
