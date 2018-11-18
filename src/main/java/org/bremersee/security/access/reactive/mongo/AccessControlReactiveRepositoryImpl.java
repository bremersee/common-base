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

package org.bremersee.security.access.reactive.mongo;

import java.io.Serializable;
import org.bremersee.security.access.AccessControl;
import org.bremersee.security.access.mongo.AccessControlEntity;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

/**
 * @author Christian Bremer
 */
public class AccessControlReactiveRepositoryImpl implements AccessControlReactiveRepositoryCustom {

  private ReactiveMongoOperations mongoOperations;

  public AccessControlReactiveRepositoryImpl(
      ReactiveMongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  @Override
  public Mono<? extends AccessControl> findAccessControl(Serializable targetId, String targetType) {
    final Query query = Query.query(new Criteria().andOperator(
        Criteria.where("targetId").is(String.valueOf(targetId)),
        Criteria.where("targetType").is(targetType)));
    return mongoOperations.findOne(query, AccessControlEntity.class);
  }
}
