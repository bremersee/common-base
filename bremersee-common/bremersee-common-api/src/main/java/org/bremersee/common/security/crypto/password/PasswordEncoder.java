/**
 * 
 */
package org.bremersee.common.security.crypto.password;

/**
 * @author Christian Bremer
 *
 */
public interface PasswordEncoder {
    
    String encode(CharSequence rawPassword);
    
    boolean matches(CharSequence rawPassword, String encodedPassword);
    

    boolean userPasswordMatches(byte[] userPassword, String clearPassword);

    byte[] createUserPassword(String clearPassword);

    String userPasswordToString(byte[] userPassword);

    byte[] userPasswordToBytes(String userPassword);

    String createSambaLMPassword(String clearPassword);
    
    String createSambaNTPassword(String clearPassword);

}
