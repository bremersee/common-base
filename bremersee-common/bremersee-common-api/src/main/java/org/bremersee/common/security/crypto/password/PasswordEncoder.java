/*
 * Copyright 2015 the original author or authors.
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

package org.bremersee.common.security.crypto.password;

/**
 * A one-way password encoder.
 *
 * @author Christian Bremer
 */
public interface PasswordEncoder {

    /**
     * Encodes the clear password.
     *
     * @param clearPassword the clear password
     * @return the encoded password
     */
    String encode(CharSequence clearPassword);

    /**
     * Checks if the passwords match.
     *
     * @param clearPassword   the clear password
     * @param encodedPassword the encoded password
     * @return {@code true} if the passwords match otherwise {@code false}
     */
    boolean matches(CharSequence clearPassword, String encodedPassword);

    /**
     * Creates a 'userPassword' of an OpenLDAP entry. Normally it's encoded.
     *
     * @param clearPassword the clear password
     * @return the encoded password
     */
    byte[] createUserPassword(String clearPassword);

    /**
     * Checks if the passwords match.
     *
     * @param userPassword  the 'userPassword' of an OpenLDAP entry
     * @param clearPassword the clear password
     * @return {@code true} if the passwords match otherwise {@code false}
     */
    boolean userPasswordMatches(byte[] userPassword, String clearPassword);

    /**
     * Transforms the encoded password into a string representation.
     *
     * @param userPassword the 'userPassword' of an OpenLDAP entry
     * @return the string representation of the encoded password
     */
    String userPasswordToString(byte[] userPassword);

    /**
     * Transforms the string representation into an encoded password.
     *
     * @param userPassword the string representation of the encoded password
     * @return the encoded password as byte array
     */
    byte[] userPasswordToBytes(String userPassword);

    /**
     * Creates an encoded Samba LM Password for using in an OpenLDAP directory.
     *
     * @param clearPassword the clear password
     * @return the Samba LM Password
     */
    String createSambaLMPassword(String clearPassword);

    /**
     * Creates an encoded Samba NT Password for using in an OpenLDAP directory.
     *
     * @param clearPassword the clear password
     * @return the Samba NT Password
     */
    String createSambaNTPassword(String clearPassword);

}
