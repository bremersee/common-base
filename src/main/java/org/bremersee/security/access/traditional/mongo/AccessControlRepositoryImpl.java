/*
 * Copyright 2018 the original author or authors.
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

package org.bremersee.security.access.traditional.mongo;

import java.io.Serializable;
import java.util.Optional;
import org.bremersee.security.access.AccessControl;
import org.bremersee.security.access.mongo.AccessControlEntity;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author Christian Bremer
 */
public class AccessControlRepositoryImpl implements AccessControlRepositoryCustom {

  private MongoOperations mongoOperations;

  public AccessControlRepositoryImpl(
      MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  @Override
  public Optional<? extends AccessControl> findAccessControl(
      Serializable targetId, String targetType) {

    final Query query = Query.query(new Criteria().andOperator(
        Criteria.where("targetId").is(String.valueOf(targetId)),
        Criteria.where("targetType").is(targetType)));

    final AccessControlEntity entity = mongoOperations.findOne(query, AccessControlEntity.class);
    if (entity != null) {
      return Optional.of(entity);
    }
    return Optional.empty();
  }
}
