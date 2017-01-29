/*
 * Copyright 2016 the original author or authors.
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

package org.bremersee.common.exception;

/**
 * @author Christian Bremer
 */
public class HttpErrorException extends RuntimeException implements StatusCodeAwareException {

    private static final long serialVersionUID = 1L;

    private int httpStatusCode; // NOSONAR

    private int customStatusCode; // NOSONAR

    /**
     * Default constructor.
     */
    public HttpErrorException() {
        this("HTTP ERROR");
    }

    /**
     * @param message the error message
     */
    public HttpErrorException(String message) {
        super(message);
    }

    /**
     * @param cause the error cause
     */
    public HttpErrorException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message
     * @param cause   the error cause
     */
    public HttpErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public int getCustomStatusCode() {
        return customStatusCode;
    }

    public void setCustomStatusCode(int customStatusCode) {
        this.customStatusCode = customStatusCode;
    }

}
