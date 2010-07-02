package org.springframework.crypto.cipher;

import javax.crypto.Cipher;

/**
 * @author Rob Harrop
 */
public class CipherTemplate {
    private Cipher cipher;

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }
}
