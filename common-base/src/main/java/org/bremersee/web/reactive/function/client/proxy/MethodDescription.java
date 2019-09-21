/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.web.reactive.function.client.proxy;

import java.lang.reflect.Method;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

/**
 * @author Christian Bremer
 */
@Getter
@ToString
@EqualsAndHashCode
class MethodDescription {

  private final String methodName;

  private final Class<?>[] parameterTypes;

  private final Class<?> responseType;

  MethodDescription(final Method method) {
    Assert.notNull(method, "Method must not be null");
    this.methodName = method.getName();
    this.parameterTypes = method.getParameterTypes();
    this.responseType = method.getReturnType();
  }

}
