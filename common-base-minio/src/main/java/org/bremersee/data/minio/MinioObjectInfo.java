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

import java.time.OffsetDateTime;

/**
 * The minio object info interface.
 *
 * @author Christian Bremer
 */
public interface MinioObjectInfo extends MinioObjectId {

  /**
   * Gets bucket.
   *
   * @return the bucket
   */
  String getBucket();

  /**
   * Gets region.
   *
   * @return the region
   */
  String getRegion();

  /**
   * Gets etag.
   *
   * @return the etag
   */
  String getEtag();

  /**
   * Gets created time.
   *
   * @return the created time
   */
  OffsetDateTime getCreatedTime();

}
