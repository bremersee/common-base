/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.core;

import java.lang.reflect.Method;
import javax.validation.constraints.NotNull;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.Ordered;
import org.springframework.validation.annotation.Validated;

/**
 * The ordered proxy.
 *
 * @author Christian Bremer
 */
@Validated
public abstract class OrderedProxy {

  private OrderedProxy() {
  }

  /**
   * Create a proxy of the target object that implements {@link Ordered}.
   *
   * @param <T> the type of the proxy
   * @param target the target
   * @param orderedValue the ordered value
   * @return the type of the proxy; if the target implements one or more interfaces, it must be one of these interfaces,
   *     otherwise it must be {@link Ordered}
   */
  public static <T> T create(@NotNull Object target, int orderedValue) {
    if (target instanceof Ordered && ((Ordered) target).getOrder() == orderedValue) {
      //noinspection unchecked
      return (T) target;
    }
    ProxyFactory proxyFactory = new ProxyFactory(target);
    if (!(target instanceof Ordered)) {
      proxyFactory.addInterface(Ordered.class);
    }
    proxyFactory.addAdvice((MethodInterceptor) methodInvocation -> {
      Method method = methodInvocation.getMethod();
      if (method.getName().equals("getOrder") && method.getParameterCount() == 0) {
        return orderedValue;
      } else {
        return methodInvocation.proceed();
      }
    });
    //noinspection unchecked
    return (T) proxyFactory.getProxy();
  }

}
