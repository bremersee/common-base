/**
 * 
 */
package org.bremersee.common.security.crypto.password;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bremersee.utils.CodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Bremer
 *
 */
public class PasswordEncoderImpl implements PasswordEncoder {

    private static final String NO_ENCRYPTION = "clear";
    
    private static final String BOUNCY_CASTLER_PROVIDER = "org.bouncycastle.jce.provider.BouncyCastleProvider";
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private int randomSaltLength = 4;
    
    private String algorithm = "SSHA";
    
    private Provider bouncyCastleProvider;
    
    public PasswordEncoderImpl() {
    }
    
    public PasswordEncoderImpl(String algorithm) {
        setAlgorithm(algorithm);
    }
    
    public PasswordEncoderImpl(String algorithm, int randomSaltLength) {
        setAlgorithm(algorithm);
        setRandomSaltLength(randomSaltLength);
    }
    
    public PasswordEncoderImpl(PasswordEncoderConfig config) {
        if (config != null) {
            setAlgorithm(config.getAlgorithm());
            setRandomSaltLength(config.getRandomSaltLength());
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s [algorithm = %s]", getClass().getName(), algorithm);
    }
    
    @PostConstruct
    public void init() {
        log.info("Initializing " + getClass().getSimpleName() + " ...");
        if (bouncyCastleProvider == null) {
            try {
                bouncyCastleProvider = (Provider) Class.forName(BOUNCY_CASTLER_PROVIDER).newInstance();
            } catch (Exception e) {
                log.warn("BouncyCastleProvider is not available - some methods won't work!");
            }
        }
        if (bouncyCastleProvider != null) {
            log.info("bouncyCastleProvider = " + bouncyCastleProvider.getClass().getName());
        }
        if (!NO_ENCRYPTION.equalsIgnoreCase(algorithm)) {
            try {
                getMessageDigest(algorithm);
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("Algorithm [" + algorithm + "] is not supported.");
            }
            
            log.info("algorithm = " + algorithm);
        } else {
            log.warn("No encryption algorithm is specified. Passwords won't be encryptet! It is better to set algorithm to 'SSHA'.");
        }
        log.info("Initializing " + getClass().getSimpleName() + " ... DONE!");
    }
    
    /**
     * @param algorithm the algorithm to set, default is <code>SSHA</code>, set to <code>clear</code> to use no encryption
     */
    public void setAlgorithm(String algorithm) {
        if (StringUtils.isBlank(algorithm) || NO_ENCRYPTION.equalsIgnoreCase(algorithm)){
            this.algorithm = NO_ENCRYPTION;
        } else {
            this.algorithm = algorithm.toUpperCase();
        }
    }
    
    public void setRandomSaltLength(int randomSaltLength) {
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
    
    public void setBouncyCastleProvider(Provider bouncyCastleProvider) {
        this.bouncyCastleProvider = bouncyCastleProvider;
    }
    
    public String encode(CharSequence rawPassword) {

        byte[] userPassword = createUserPassword(rawPassword == null ? null : rawPassword.toString());
        return userPasswordToString(userPassword);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {

        String clearPassword = rawPassword != null ? rawPassword.toString() : null;
        byte[] userPassword = encodedPassword != null ? userPasswordToBytes(encodedPassword) : null;
        return userPasswordMatches(userPassword, clearPassword);
    }
    
    private byte[] getRandomSalt() {
        byte[] b = new byte[randomSaltLength];
        for(int i = 0; i < randomSaltLength; i++){
            byte bt = (byte)(((Math.random())*256)-128);
            b[i] = bt;
        }
        return b;
    }

    private boolean isSaltedSha(String algorithm) {
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
        if (isSaltedSha(algorithm)) {
            algorithm = algorithm.substring(1).toUpperCase();
        } else {
            algorithm = algorithm.toUpperCase();
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            
        } catch (NoSuchAlgorithmException e) {
            try {
                messageDigest = MessageDigest.getInstance(algorithm, getBouncyCastleProvider());
                
            } catch (NoSuchAlgorithmException e1) {
                throw new UnsupportedOperationException(e);
            }
        }
        return messageDigest;
    }
    
    private String[] getAlgorithmAndPasswort(byte[] userPassword) {
        String[] ap = new String[2];
        if (userPassword == null) {
            ap[0] = NO_ENCRYPTION;
            ap[1] = null;
            return ap;
        }
        String tmp =  CodingUtils.toStringSilently(userPassword, StandardCharsets.UTF_8);
        int i1 = tmp.indexOf('{');
        int i2 = tmp.indexOf('}');
        if (i1 == 0 && i1 < i2 && i2 < tmp.length()-1) {
            ap[0] = tmp.substring(1, i2);
            ap[1] = tmp.substring(i2 + 1);
            return ap;
        }
        ap[0] = NO_ENCRYPTION;
        ap[1] = tmp;
        return ap;
    }
    
    @Override
    public boolean userPasswordMatches(byte[] userPassword, String clearPassword) {
        
        if (userPassword == null && StringUtils.isBlank(clearPassword)) {
            return true;
        }
        
        if (userPassword == null || StringUtils.isBlank(clearPassword)) {
            return false;
        }
        
        final String[] ap = getAlgorithmAndPasswort(userPassword);
        final String algorithm = ap[0];
        final String password = ap[1];
        
        if (NO_ENCRYPTION.equalsIgnoreCase(algorithm)) {
            if (StringUtils.isBlank(clearPassword) && password == null) {
                return true;
            }
            return clearPassword.equals(password);
        }
        
        MessageDigest md = getMessageDigest(algorithm);
        
        if (isSaltedSha(algorithm)) {

            // extract the SHA hashed data into hs[0]
            // extract salt into hs[1]
            byte[][] hs = split(Base64.decodeBase64(password), getPasswordHashSize(algorithm));
            byte[] hash = hs[0];
            byte[] salt = hs[1];

            // Update digest object with byte array of clear text string and salt
            md.reset();
            md.update(clearPassword.getBytes());
            md.update(salt);

            // Complete hash computation, this is now binary data
            byte[] pwhash = md.digest();

            if (log.isDebugEnabled()) {
                log.debug("Salted Hash extracted (in hex): " + Hex.encodeHexString(hash));
                log.debug("Salt extracted (in hex): " + Hex.encodeHexString(salt));
                log.debug("Hash length is: " + hash.length);
                log.debug("Salt length is: " + salt.length);
                log.debug("Salted Hash presented in hex: " + Hex.encodeHexString(pwhash));
            }

            return MessageDigest.isEqual(hash, pwhash);
        }
        
        byte[] digest = md.digest(CodingUtils.toBytesSilently(clearPassword, StandardCharsets.UTF_8));
        return MessageDigest.isEqual(digest, Base64.decodeBase64(password));
    }
    
    @Override
    public byte[] createUserPassword(String clearPassword) {
        
        if (clearPassword == null) {
            return null;
        }
        
        if (NO_ENCRYPTION.equalsIgnoreCase(algorithm)) {
            return CodingUtils.toBytesSilently(clearPassword, StandardCharsets.UTF_8);
        }
        
        MessageDigest md = getMessageDigest(algorithm);
        
        byte[] digest;
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

        StringBuilder sb = new StringBuilder();
        sb.append("{").append(algorithm).append("}");
        sb.append(CodingUtils.toStringSilently(Base64.encodeBase64(digest), StandardCharsets.UTF_8));
        return CodingUtils.toBytesSilently(sb.toString(), StandardCharsets.UTF_8);
    }
    
    @Override
    public String userPasswordToString(byte[] userPassword) {
        if (userPassword == null) {
            return null;
        }
        return CodingUtils.toStringSilently(userPassword, StandardCharsets.UTF_8);
    }
    
    @Override
    public byte[] userPasswordToBytes(String userPassword) {
        if (userPassword == null) {
            return null;
        }
        return CodingUtils.toBytesSilently(userPassword, StandardCharsets.UTF_8);
    }
    
    public String createSambaLMPassword(String clearPassword) {
        if (clearPassword == null) {
            return null;
        }
        try {
            // Gets the first 14-bytes of the ASCII upper cased password
            int len = clearPassword.length();
            if (len > 14)
                len = 14;
            Cipher c = Cipher.getInstance("DES/ECB/NoPadding");

            byte[] lm_pw = new byte[14];
            byte[] bytes = clearPassword.toUpperCase().getBytes();
            int i;
            for (i = 0; i < len; i++)
                lm_pw[i] = bytes[i];
            for (; i < 14; i++)
                lm_pw[i] = 0;

            byte[] lm_hpw = new byte[16];
            // Builds a first DES key with its first 7 bytes
            Key k = computeDESKey(lm_pw, 0);
            c.init(Cipher.ENCRYPT_MODE, k);
            // Hashes the MAGIC number with this key into the first 8 bytes of
            // the result
            c.doFinal(MAGIC, 0, 8, lm_hpw, 0);

            // Repeats the work with the last 7 bytes to gets the last 8 bytes
            // of the result
            k = computeDESKey(lm_pw, 7);
            c.init(Cipher.ENCRYPT_MODE, k);
            c.doFinal(MAGIC, 0, 8, lm_hpw, 8);

            // return lm_hpw;
            
            return Hex.encodeHexString(lm_hpw).toUpperCase();

        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeyException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeySpecException e) {
            throw new UnsupportedOperationException(e);
        } catch (ShortBufferException e) {
            throw new UnsupportedOperationException(e);
        } catch (IllegalBlockSizeException e) {
            throw new UnsupportedOperationException(e);
        } catch (BadPaddingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public String createSambaNTPassword(String clearPassword) {

        if (clearPassword == null) {
            return null;
        }
        
        // Gets the first 14-bytes of the UNICODE password
        int len = clearPassword.length();
        if (len > 14)
            len = 14;
        byte[] nt_pw = new byte[2 * len];
        for (int i = 0; i < len; i++) {
            char ch = clearPassword.charAt(i);
            nt_pw[2 * i] = getLoByte(ch);
            nt_pw[2 * i + 1] = getHiByte(ch);
        }

        MessageDigest md4;
        try {
            md4 = MessageDigest.getInstance("MD4", getBouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
        byte[] ntHash = md4.digest(nt_pw);

        return Hex.encodeHexString(ntHash).toUpperCase();
    }

    /**
     * The magic number used to compute the Lan Manager hashed password.
     */
    protected static final byte[] MAGIC = new byte[] { 0x4B, 0x47, 0x53, 0x21,
            0x40, 0x23, 0x24, 0x25 };

    /**
     * <p>
     * Computes an odd DES key from 56 bits represented as a 7-bytes array.
     * </p>
     * <p>
     * Keeps elements from index <code>offset</code> to index
     * <code>offset + 7</code> of supplied array.
     * </p>
     *
     * @param keyData
     *            a byte array containing the 56 bits used to compute the DES
     *            key
     * @param offset
     *            the offset of the first element of the 56-bits key data
     *
     * @return the odd DES key generated
     *
     * @exception InvalidKeyException
     * @exception NoSuchAlgorithmException
     * @exception InvalidKeySpecException
     */
    protected static Key computeDESKey(byte[] keyData, int offset)
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
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        return secretKey;
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
    protected static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    protected static byte getLoByte(char c) {
        return (byte) c;
    }

    protected static byte getHiByte(char c) {
        return (byte) ((c >>> 8) & 0xFF);
    }

    
    /**
     * split a byte array in two
     *
     * @param src
     *      byte array to be split
     * @param n
     *      element at which to split the byte array
     * @return byte[][] two byte arrays that have been split
     */
    private static byte[][] split(byte[] src, int n) {
        byte[] l, r;
        if (src == null || src.length <= n) {
            l = src;
            r = new byte[0];
        } else {
            l = new byte[n];
            r = new byte[src.length - n];
            System.arraycopy(src, 0, l, 0, n);
            System.arraycopy(src, n, r, 0, r.length);
        }
        byte[][] lr = { l, r };
        return lr;
    }

    /**
     * Combine two byte arrays
     *
     * @param l
     *      first byte array
     * @param r
     *      second byte array
     * @return byte[] combined byte array
     */
    private static byte[] concatenate(byte[] l, byte[] r) {
        byte[] b = new byte[l.length + r.length];
        System.arraycopy(l, 0, b, 0, l.length);
        System.arraycopy(r, 0, b, l.length, r.length);
        return b;
    }
    
}
