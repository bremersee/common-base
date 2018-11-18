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

package org.bremersee.security.access;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public class DomainObjectReferenceCreatorImpl implements DomainObjectReferenceCreator {

  private final String methodName;

  public DomainObjectReferenceCreatorImpl() {
    this.methodName = "getId";
  }

  public DomainObjectReferenceCreatorImpl(String methodName) {
    if (StringUtils.hasText(methodName)) {
      this.methodName = methodName;
    } else {
      this.methodName = "getId";
    }
  }

  @Override
  public Optional<DomainObjectReference> createReference(Object domainObject) {
    if (domainObject == null) {
      return Optional.empty();
    }
    final Method method = ReflectionUtils.findMethod(domainObject.getClass(), methodName);
    if (method == null) {
      return Optional.empty();
    }
    Object id = ReflectionUtils.invokeMethod(method, domainObject);
    if (!(id instanceof Serializable)) {
      return Optional.empty();
    }
    return Optional.of(new DomainObjectReferenceImpl(
        (Serializable) id,
        domainObject.getClass().getName()));
  }

}
