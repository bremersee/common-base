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

package org.bremersee.data.convert;

import org.bremersee.common.model.TimeZoneId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

/**
 * The time zone id write converter.
 *
 * @author Christian Bremer
 */
@WritingConverter
public class TimeZoneIdWriteConverter implements Converter<TimeZoneId, String> {

  @Override
  public String convert(TimeZoneId source) {
    return source.toString();
  }
}
