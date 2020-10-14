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

package org.bremersee.converter;

import java.util.Locale;
import lombok.ToString;
import org.bremersee.common.model.JavaLocale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * The string to locale converter.
 *
 * @author Christian Bremer
 */
@ToString
public class StringToLocaleConverter implements Converter<String, Locale> {

  @Override
  public Locale convert(@NonNull String source) {
    return JavaLocale.fromValue(source).toLocale();
  }

}
