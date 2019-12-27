package org.bremersee.converter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

/**
 * The base common converters test.
 */
public class BaseCommonConvertersTest {

  /**
   * Register all.
   */
  @Test
  public void registerAll() {
    FormatterRegistry registry = mock(FormatterRegistry.class);
    BaseCommonConverters.registerAll(registry);
    verify(registry, times(BaseCommonConverters.CONVERTERS.length))
        .addConverter(any(Converter.class));
  }
}