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

package org.bremersee.common.exception;

/**
 * @author Christian Bremer
 */
public class UserAlreadyExistsException extends AlreadyExistsException {

    public UserAlreadyExistsException() {
        this(Exceptions.USER_ALREADY_EXISTS.getDefaultMessage());
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(Throwable cause) {
        this(Exceptions.USER_ALREADY_EXISTS.getDefaultMessage(), cause);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public int getHttpStatusCode() {
        return Exceptions.USER_ALREADY_EXISTS.getHttpStatusCode();
    }

    @Override
    public int getCustomStatusCode() {
        return Exceptions.USER_ALREADY_EXISTS.getCustomStatusCode();
    }

}
