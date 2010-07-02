package org.springframework.crypto.digest;

/**
 * @author Rob Harrop
 */
public interface SaltProvider {
    byte[] newSalt(int size);
}
