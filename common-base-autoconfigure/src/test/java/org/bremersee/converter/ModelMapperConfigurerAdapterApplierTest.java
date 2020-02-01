package org.bremersee.converter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The model mapper configurer adapter applier test.
 *
 * @author Christian Bremer
 */
class ModelMapperConfigurerAdapterApplierTest {

  /**
   * Tests init method that calls the adapters.
   */
  @Test
  void init() {
    ModelMapperConfigurerAdapter adapter = mock(ModelMapperConfigurerAdapter.class);

    ModelMapperConfigurerAdapterApplier applier = new ModelMapperConfigurerAdapterApplier(
        objectProvider(new ModelMapper()),
        objectProvider(Collections.singletonList(adapter)));

    applier.init();
    verify(adapter).configure(any(ModelMapper.class));
  }

  /**
   * Tests init method that does not call the adapters, because there is no model mapper.
   */
  @Test
  void initWithNoModelMapper() {
    ModelMapperConfigurerAdapter adapter = mock(ModelMapperConfigurerAdapter.class);

    ModelMapperConfigurerAdapterApplier applier = new ModelMapperConfigurerAdapterApplier(
        objectProvider(null),
        objectProvider(Collections.singletonList(adapter)));

    applier.init();
    verify(adapter, never()).configure(any(ModelMapper.class));
  }

  private static <T> ObjectProvider<T> objectProvider(T provides) {
    //noinspection unchecked
    ObjectProvider<T> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(provides);
    return provider;
  }
}