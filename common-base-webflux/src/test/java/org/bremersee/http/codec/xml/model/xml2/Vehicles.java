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

package org.bremersee.http.codec.xml.model.xml2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The vehicles.
 *
 * @author Christian Bremer
 */
@XmlRootElement(name = "vehicles")
@XmlType(name = "vehiclesType")
@XmlAccessorType(XmlAccessType.FIELD)
public class Vehicles {

  @XmlAttribute(required = true)
  private String series;

  @XmlAttribute(required = true)
  private int year;

  @XmlElement(defaultValue = "0")
  private Integer month;

  @XmlElementWrapper(name = "list")
  @XmlElement(name = "vehicle")
  private List<Vehicle> entries = new ArrayList<>();

  /**
   * Gets series.
   *
   * @return the series
   */
  @SuppressWarnings("unused")
  public String getSeries() {
    return series;
  }

  /**
   * Sets series.
   *
   * @param series the series
   */
  public void setSeries(String series) {
    this.series = series;
  }

  /**
   * Gets year.
   *
   * @return the year
   */
  @SuppressWarnings("unused")
  public int getYear() {
    return year;
  }

  /**
   * Sets year.
   *
   * @param year the year
   */
  public void setYear(int year) {
    this.year = year;
  }

  /**
   * Gets month.
   *
   * @return the month
   */
  @SuppressWarnings("unused")
  public Integer getMonth() {
    return month;
  }

  /**
   * Sets month.
   *
   * @param month the month
   */
  public void setMonth(Integer month) {
    this.month = month;
  }

  /**
   * Gets entries.
   *
   * @return the entries
   */
  public List<Vehicle> getEntries() {
    return entries;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vehicles vehicles = (Vehicles) o;
    return year == vehicles.year
        && Objects.equals(series, vehicles.series)
        && Objects.equals(month, vehicles.month)
        && Objects.equals(entries, vehicles.entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(series, year, month, entries);
  }

  @Override
  public String toString() {
    return "Vehicles{"
        + "series='" + series + '\''
        + ", year=" + year
        + ", month=" + month
        + '}';
  }
}
