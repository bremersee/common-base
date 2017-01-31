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

package org.bremersee.common.spring.autoconfigure;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties("bremersee.web-mvc")
@Data
@NoArgsConstructor
public class WebMvcProperties {

	private ErrorMessageDetails errorMessageType = ErrorMessageDetails.FULL;
	
	private Map<String, ErrorMessageDetails> errorMessageDetailsHandlerMap = new HashMap<>();
	
	public enum ErrorMessageDetails {
		LIGHTWEIGHT, FULL;
	}
}