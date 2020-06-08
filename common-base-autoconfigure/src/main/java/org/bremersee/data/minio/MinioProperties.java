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

package org.bremersee.data.minio;

import java.time.Duration;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The minio properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.minio")
@Getter
@Setter
@ToString(exclude = {"accessKey", "secretKey"})
@EqualsAndHashCode
@NoArgsConstructor
@Validated
public class MinioProperties {

  /**
   * The minio url.
   */
  private String url;

  /**
   * The access key.
   */
  private String accessKey;

  /**
   * The secret key.
   */
  private String secretKey;

  /**
   * The connect timeout.
   */
  @NotNull
  private Duration connectTimeout = Duration.ofSeconds(10);

  /**
   * The write timeout.
   */
  @NotNull
  private Duration writeTimeout = Duration.ofSeconds(60);

  /**
   * The read timeout.
   */
  @NotNull
  private Duration readTimeout = Duration.ofSeconds(10);

  /**
   * Metric configuration prefix which are registered on Actuator.
   */
  private String metricName = "minio.storage";

}
