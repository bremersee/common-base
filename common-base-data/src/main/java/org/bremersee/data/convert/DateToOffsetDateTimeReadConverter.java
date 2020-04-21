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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import lombok.ToString;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * The date to offset date time read converter.
 *
 * @author Christian Bremer
 */
@ReadingConverter
@ToString
public class DateToOffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {

  @Override
  public OffsetDateTime convert(Date date) {
    return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
  }

}
