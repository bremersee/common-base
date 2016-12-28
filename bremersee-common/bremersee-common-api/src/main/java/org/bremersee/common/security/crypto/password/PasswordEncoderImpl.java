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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bremersee.utils.CodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * An one-way password encoder.
 *
 * @author Christian Bremer
 */
public class PasswordEncoderImpl implements PasswordEncoder {

    private static final String NO_ENCRYPTION = "clear";

    private static final String BOUNCY_CASTLER_PROVIDER = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The magic number used to compute the Lan Manager hashed password.
     */
    private static final byte[] MAGIC = new byte[]{
            0x4B, 0x47, 0x53, 0x21,
            0x40, 0x23, 0x24, 0x25};

    private int randomSaltLength = 4;

    private String algorithm = "SSHA";

    private Provider bouncyCastleProvider;

    /**
     * Default constructor.
     */
    public PasswordEncoderImpl() {
        super();
    }

    /**
     * Password encoder that uses the specified algorithm.
     *
     * @param algorithm the encoding algorithm
     */
    public PasswordEncoderImpl(final String algorithm) {
        setAlgorithm(algorithm);
    }

    /**
     * Password encoder that uses the specified algorithm and random salt length.
     *
     * @param algorithm        the encoding algorithm
     * @param randomSaltLength the random salt length
     */
    public PasswordEncoderImpl(final String algorithm, final int randomSaltLength) {
        setAlgorithm(algorithm);
        setRandomSaltLength(randomSaltLength);
    }

    /**
     * Password encoder that uses the specified configuration.
     *
     * @param config the password encoder configuration
     */
    public PasswordEncoderImpl(final PasswordEncoderConfig config) {
        if (config != null) {
            setAlgorithm(config.getAlgorithm());
            setRandomSaltLength(config.getRandomSaltLength());
        }
    }

    @Override
    public String toString() {
        return String.format("%s [algorithm = %s]", getClass().getName(), algorithm);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
        result = prime * result + randomSaltLength;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PasswordEncoderImpl other = (PasswordEncoderImpl) obj;
        if (algorithm == null) {
            if (other.algorithm != null)
                return false;
        } else if (!algorithm.equals(other.algorithm))
            return false;
        return randomSaltLength == other.randomSaltLength;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing " + getClass().getSimpleName() + " ...");
        if (bouncyCastleProvider == null) {
            try {
                bouncyCastleProvider = (Provider) Class.forName(BOUNCY_CASTLER_PROVIDER).newInstance();
            } catch (Exception e) { // NOSONAR
                log.warn("BouncyCastleProvider is not available - some methods won't work!");
            }
        }
        if (bouncyCastleProvider != null) {
            log.info("bouncyCastleProvider = " + bouncyCastleProvider.getClass().getName());
        }
        if (!NO_ENCRYPTION.equalsIgnoreCase(algorithm)) {
            try {
                getMessageDigest(algorithm);
            } catch (UnsupportedOperationException e) { // NOSONAR
                throw new IllegalArgumentException("Algorithm [" + algorithm + "] is not supported.");
            }

            log.info("algorithm = " + algorithm);
        } else {
            log.warn("No encryption algorithm is specified. Passwords won't be encrypted! " +
                    "It is better to set algorithm to 'SSHA'.");
        }
        log.info("Initializing " + getClass().getSimpleName() + " ... DONE!");
    }

    /**
     * @param algorithm the algorithm to set, default is <code>SSHA</code>,
     *                  set to <code>clear</code> to use no encryption
     */
    public void setAlgorithm(final String algorithm) {
        if (StringUtils.isBlank(algorithm) || NO_ENCRYPTION.equalsIgnoreCase(algorithm)) {
            this.algorithm = NO_ENCRYPTION;
        } else {
            this.algorithm = algorithm.toUpperCase();
        }
    }

    public void setRandomSaltLength(final int randomSaltLength) {
        this.randomSaltLength = randomSaltLength;
    }

    protected Provider getBouncyCastleProvider() {
        if (bouncyCastleProvider == null) {
            try {
                bouncyCastleProvider = (Provider) Class.forName(BOUNCY_CASTLER_PROVIDER).newInstance();
            } catch (Exception e) {
                throw new UnsupportedOperationException(e);
            }
        }
        return bouncyCastleProvider;
    }

    public void setBouncyCastleProvider(final Provider bouncyCastleProvider) {
        this.bouncyCastleProvider = bouncyCastleProvider;
    }

    @Override
    public String encode(final CharSequence clearPassword) {

        final byte[] userPassword = createUserPassword(clearPassword == null ? null : clearPassword.toString());
        return userPasswordToString(userPassword);
    }

    @Override
    public boolean matches(final CharSequence clearPassword, final String encodedPassword) {

        final String checkedClearPassword = clearPassword != null ? clearPassword.toString() : null;
        final byte[] userPassword = encodedPassword != null ? userPasswordToBytes(encodedPassword) : null;
        return userPasswordMatches(userPassword, checkedClearPassword);
    }

    private byte[] getRandomSalt() {
        final byte[] b = new byte[randomSaltLength];
        for (int i = 0; i < randomSaltLength; i++) {
            byte bt = (byte) (((Math.random()) * 256) - 128);
            b[i] = bt;
        }
        return b;
    }

    private boolean isSaltedSha(final String algorithm) {
        return algorithm != null && algorithm.toUpperCase().startsWith("SSHA");
    }

    private int getPasswordHashSize(String algorithm) {
        if (isSaltedSha(algorithm)) {
            if (algorithm.endsWith("256")) {
                return 32;
            }
            if (algorithm.endsWith("384")) {
                return 48;
            }
            if (algorithm.endsWith("512")) {
                return 64;
            }
            return 20;
        }
        return 0;
    }

    private MessageDigest getMessageDigest(String algorithm) {
        String checkedAlgorithm = algorithm;
        if (isSaltedSha(checkedAlgorithm)) {
            checkedAlgorithm = checkedAlgorithm.substring(1).toUpperCase();
        } else {
            checkedAlgorithm = checkedAlgorithm.toUpperCase(); // NOSONAR
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(checkedAlgorithm);

        } catch (NoSuchAlgorithmException e) {
            try {
                messageDigest = MessageDigest.getInstance(checkedAlgorithm, getBouncyCastleProvider());

            } catch (NoSuchAlgorithmException e1) { // NOSONAR
                throw new UnsupportedOperationException(e);
            }
        }
        return messageDigest;
    }

    private String[] getAlgorithmAndPassword(final byte[] userPassword) {
        final String[] ap = new String[2];
        if (userPassword == null) {
            ap[0] = NO_ENCRYPTION;
            ap[1] = null;
            return ap;
        }
        final String tmp = CodingUtils.toStringSilently(userPassword, StandardCharsets.UTF_8);
        final int i1 = tmp.indexOf('{');
        final int i2 = tmp.indexOf('}');
        if (i1 == 0 && i1 < i2 && i2 < tmp.length() - 1) {
            ap[0] = tmp.substring(1, i2);
            ap[1] = tmp.substring(i2 + 1);
            return ap;
        }
        ap[0] = NO_ENCRYPTION;
        ap[1] = tmp;
        return ap;
    }

    @Override
    public boolean userPasswordMatches(final byte[] userPassword, final String clearPassword) {

        if (userPassword == null && StringUtils.isBlank(clearPassword)) {
            return true;
        }

        if (userPassword == null || StringUtils.isBlank(clearPassword)) {
            return false;
        }

        final String[] ap = getAlgorithmAndPassword(userPassword);
        final String localAlgorithm = ap[0];
        final String password = ap[1];

        if (NO_ENCRYPTION.equalsIgnoreCase(localAlgorithm)) {
            return StringUtils.isBlank(clearPassword) && password == null || clearPassword.equals(password);
        }

        final MessageDigest md = getMessageDigest(localAlgorithm);

        if (isSaltedSha(localAlgorithm)) {

            // extract the SHA hashed data into hs[0]
            // extract salt into hs[1]
            final byte[][] hs = split(Base64.decodeBase64(password), getPasswordHashSize(localAlgorithm));
            final byte[] hash = hs[0];
            final byte[] salt = hs[1];

            // Update digest object with byte array of clear text string and salt
            md.reset();
            md.update(clearPassword.getBytes());
            md.update(salt);

            // Complete hash computation, this is now binary data
            final byte[] pwhash = md.digest();

            if (log.isDebugEnabled()) {
                log.debug("Salted Hash extracted (in hex): " + Hex.encodeHexString(hash));
                log.debug("Salt extracted (in hex): " + Hex.encodeHexString(salt));
                log.debug("Hash length is: " + hash.length);
                log.debug("Salt length is: " + salt.length);
                log.debug("Salted Hash presented in hex: " + Hex.encodeHexString(pwhash));
            }

            final boolean result = MessageDigest.isEqual(hash, pwhash);
            log.debug("Password matches? " + result);
            return result;
        }

        final byte[] digest = md.digest(CodingUtils.toBytesSilently(clearPassword, StandardCharsets.UTF_8));
        return MessageDigest.isEqual(digest, Base64.decodeBase64(password));
    }

    @Override
    public byte[] createUserPassword(final String clearPassword) {

        if (clearPassword == null) {
            return null; // NOSONAR
        }

        if (NO_ENCRYPTION.equalsIgnoreCase(algorithm)) {
            return CodingUtils.toBytesSilently(clearPassword, StandardCharsets.UTF_8);
        }

        final MessageDigest md = getMessageDigest(algorithm);

        final byte[] digest;
        if (isSaltedSha(algorithm)) {

            byte[] salt = getRandomSalt();

            md.reset();
            md.update(CodingUtils.toBytesSilently(clearPassword, StandardCharsets.UTF_8));
            md.update(salt);

            // Complete hash computation, this results in binary data
            byte[] pwhash = md.digest();

            digest = concatenate(pwhash, salt);

        } else {

            digest = md.digest(CodingUtils.toBytesSilently(clearPassword, StandardCharsets.UTF_8));
        }

        String result = "{" + algorithm + "}" + CodingUtils.toStringSilently(
                Base64.encodeBase64(digest), StandardCharsets.UTF_8);
        return CodingUtils.toBytesSilently(result, StandardCharsets.UTF_8);
    }

    @Override
    public String userPasswordToString(final byte[] userPassword) {
        if (userPassword == null) {
            return null;
        }
        return CodingUtils.toStringSilently(userPassword, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] userPasswordToBytes(final String userPassword) {
        if (userPassword == null) {
            return null; // NOSONAR
        }
        return CodingUtils.toBytesSilently(userPassword, StandardCharsets.UTF_8);
    }

    @Override
    public String createSambaLMPassword(final String clearPassword) {
        if (clearPassword == null) {
            return null;
        }
        try {
            // Gets the first 14-bytes of the ASCII upper cased password
            int len = clearPassword.length();
            if (len > 14)
                len = 14;
            final Cipher c = Cipher.getInstance("DES/ECB/NoPadding"); // NOSONAR

            final byte[] lmPw = new byte[14];
            final byte[] bytes = clearPassword.toUpperCase().getBytes();
            int i;
            for (i = 0; i < len; i++)
                lmPw[i] = bytes[i];
            for (; i < 14; i++)
                lmPw[i] = 0;

            final byte[] lmHpw = new byte[16];
            // Builds a first DES key with its first 7 bytes
            Key k = computeDESKey(lmPw, 0);
            c.init(Cipher.ENCRYPT_MODE, k);
            // Hashes the MAGIC number with this key into the first 8 bytes of
            // the result
            c.doFinal(MAGIC, 0, 8, lmHpw, 0);

            // Repeats the work with the last 7 bytes to gets the last 8 bytes
            // of the result
            k = computeDESKey(lmPw, 7);
            c.init(Cipher.ENCRYPT_MODE, k);
            c.doFinal(MAGIC, 0, 8, lmHpw, 8);

            return Hex.encodeHexString(lmHpw).toUpperCase();

        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | InvalidKeySpecException
                | ShortBufferException
                | IllegalBlockSizeException
                | BadPaddingException e) {

            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public String createSambaNTPassword(String clearPassword) {

        if (clearPassword == null) {
            return null;
        }

        // Gets the first 14-bytes of the UNICODE password
        int len = clearPassword.length();
        if (len > 14)
            len = 14;
        byte[] ntPw = new byte[2 * len];
        for (int i = 0; i < len; i++) {
            char ch = clearPassword.charAt(i);
            ntPw[2 * i] = getLoByte(ch);
            ntPw[2 * i + 1] = getHiByte(ch);
        }

        final MessageDigest md4;
        try {
            md4 = MessageDigest.getInstance("MD4", getBouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
        final byte[] ntHash = md4.digest(ntPw);

        return Hex.encodeHexString(ntHash).toUpperCase();
    }

    /**
     * <p>
     * Computes an odd DES key from 56 bits represented as a 7-bytes array.
     * </p>
     * <p>
     * Keeps elements from index {@code offset} to index
     * {@code offset + 7} of supplied array.
     * </p>
     *
     * @param keyData a byte array containing the 56 bits used to compute the DES
     *                key
     * @param offset  the offset of the first element of the 56-bits key data
     * @return the odd DES key generated
     * @throws InvalidKeyException      when key is invalid
     * @throws NoSuchAlgorithmException when algorithm is not available
     * @throws InvalidKeySpecException  when key spec is invalid
     */
    private static Key computeDESKey(final byte[] keyData, final int offset)
            throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        byte[] desKeyData = new byte[8];
        int[] k = new int[7];

        for (int i = 0; i < 7; i++)
            k[i] = unsignedByteToInt(keyData[offset + i]);

        desKeyData[0] = (byte) (k[0] >>> 1);
        desKeyData[1] = (byte) (((k[0] & 0x01) << 6) | (k[1] >>> 2));
        desKeyData[2] = (byte) (((k[1] & 0x03) << 5) | (k[2] >>> 3));
        desKeyData[3] = (byte) (((k[2] & 0x07) << 4) | (k[3] >>> 4));
        desKeyData[4] = (byte) (((k[3] & 0x0F) << 3) | (k[4] >>> 5));
        desKeyData[5] = (byte) (((k[4] & 0x1F) << 2) | (k[5] >>> 6));
        desKeyData[6] = (byte) (((k[5] & 0x3F) << 1) | (k[6] >>> 7));
        desKeyData[7] = (byte) (k[6] & 0x7F);

        for (int i = 0; i < 8; i++)
            desKeyData[i] = (byte) (unsignedByteToInt(desKeyData[i]) << 1);

        KeySpec desKeySpec = new DESKeySpec(desKeyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(desKeySpec);
    }

    /**
     * <p>
     * Converts an unsigned byte to an unsigned integer.
     * </p>
     * <p>
     * Notice that Java bytes are always signed, but the cryptographic
     * algorithms rely on unsigned ones, that can be simulated in this way.<br>
     * A bit mask is employed to prevent that the signum bit is extended to
     * MSBs.
     * </p>
     */
    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    private static byte getLoByte(char c) {
        return (byte) c;
    }

    private static byte getHiByte(char c) {
        return (byte) ((c >>> 8) & 0xFF);
    }


    /**
     * Splits a byte array in two.
     *
     * @param src byte array to be split
     * @param n   element at which to split the byte array
     * @return byte[][] two byte arrays that have been split
     */
    private static byte[][] split(final byte[] src, final int n) {
        byte[] l;
        byte[] r;
        if (src == null || src.length <= n) {
            l = src;
            r = new byte[0];
        } else {
            l = new byte[n];
            r = new byte[src.length - n];
            System.arraycopy(src, 0, l, 0, n);
            System.arraycopy(src, n, r, 0, r.length);
        }
        return new byte[][]{l, r};
    }

    /**
     * Combine two byte arrays.
     *
     * @param l first byte array
     * @param r second byte array
     * @return byte[] combined byte array
     */
    private static byte[] concatenate(final byte[] l, final byte[] r) {
        final byte[] b = new byte[l.length + r.length];
        System.arraycopy(l, 0, b, 0, l.length);
        System.arraycopy(r, 0, b, l.length, r.length);
        return b;
    }

}
