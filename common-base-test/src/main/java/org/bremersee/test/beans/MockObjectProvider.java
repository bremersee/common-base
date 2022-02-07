/*
 * Copyright 2020-2022 the original author or authors.
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

package org.bremersee.test.beans;

import java.util.function.Consumer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The mock object provider.
 *
 * @param <T> the type parameter
 */
public class MockObjectProvider<T> implements ObjectProvider<T> {

  private final T provides;

  private final Consumer<T> dependencyConsumer;

  /**
   * Instantiates a new mock object provider.
   *
   * @param provides the provides
   */
  public MockObjectProvider(@Nullable T provides) {
    this(provides, null);
  }

  /**
   * Instantiates a new mock object provider.
   *
   * <p>If a dependency consumer is set, the given consumer will be invoked when
   * {@link ObjectProvider#ifAvailable(Consumer)} or {@link ObjectProvider#ifUnique(Consumer)} is
   * called.
   *
   * @param provides the provides
   * @param dependencyConsumer the dependency consumer
   */
  public MockObjectProvider(@Nullable T provides, @Nullable Consumer<T> dependencyConsumer) {
    this.provides = provides;
    this.dependencyConsumer = dependencyConsumer;
  }

  private void callConsumer(Consumer<T> originalConsumer) {
    if (provides == null) {
      return;
    }
    if (dependencyConsumer != null) {
      dependencyConsumer.accept(provides);
    } else {
      originalConsumer.accept(provides);
    }
  }

  @NonNull
  @Override
  public T getObject(@NonNull Object... objects) throws BeansException {
    if (provides == null) {
      throw new NoSuchBeanDefinitionException("Mocked object provider has no bean.");
    }
    return provides;
  }

  @Override
  public void ifAvailable(@NonNull Consumer<T> dependencyConsumer) throws BeansException {
    callConsumer(dependencyConsumer);
  }

  @Override
  public void ifUnique(@NonNull Consumer<T> dependencyConsumer) throws BeansException {
    callConsumer(dependencyConsumer);
  }

  @Override
  public T getIfAvailable() throws BeansException {
    return provides;
  }

  @Override
  public T getIfUnique() throws BeansException {
    return provides;
  }

  @NonNull
  @Override
  public T getObject() throws BeansException {
    if (provides == null) {
      throw new NoSuchBeanDefinitionException("Mocked object provider has no bean.");
    }
    return provides;
  }
}
