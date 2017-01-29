/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.common.converter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;

/**
 * <p>
 * Utility methods for converting.
 * </p>
 *
 * @author Christian Bremer
 */
public abstract class ConverterUtils {

    /**
     * Never construct.
     */
    private ConverterUtils() {
        super();
    }

    /**
     * Converts a {@link ChronoZonedDateTime} into a {@link Date}.
     *
     * @param dateTime the input time (can be {@code null}
     * @return the appropriate date object (or {@code null} if the inpt time was {@code null})
     */
    public static Date mapToDate(final ChronoZonedDateTime<? extends ChronoLocalDate> dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.toInstant());
    }

    /**
     * Converts a {@link Date} into a {@link ZonedDateTime}.
     *
     * @param date the input time (can be {@code null}
     * @return the appropriate zoned time object (or {@code null} if the inpt time was {@code null})
     */
    public static ZonedDateTime mapToZonedDateTime(final Date date) {
        if (date == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

}
