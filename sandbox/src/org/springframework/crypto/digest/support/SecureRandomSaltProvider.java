package org.springframework.crypto.digest.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.crypto.digest.SaltProvider;

import java.security.SecureRandom;

/**
 * @author Rob Harrop
 */
public class SecureRandomSaltProvider implements SaltProvider, InitializingBean {

    private String algorithm = "SHA1PRNG";

    private String provider;

    private SecureRandom srand;

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void afterPropertiesSet() throws Exception {
        if (provider == null) {
            srand = SecureRandom.getInstance(algorithm);
        } else {
            srand = SecureRandom.getInstance(algorithm, provider);
        }

    }

    public byte[] newSalt(int size) {
        byte[] salt = new byte[size];
        srand.nextBytes(salt);
        return salt;
    }
}
