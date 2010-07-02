package org.springframework.crypto.cipher;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.Provider;
import java.security.NoSuchAlgorithmException;

/**
 * <code>FactoryBean</code> implementation for creating instances of
 * <code>javax.crypto.Cipher</code> for use in cryptographic operations.
 * @author Rob Harrop
 */
public class CipherFactoryBean implements FactoryBean, InitializingBean {

    /**
     * Indicates whether this instance is in singleton mode.
     */
    private boolean singleton = true;

    /**
     * Caches the <code>Cipher</code> instance when in singleton mode.
     */
    private Cipher cachedCipher;

    /**
     * Transformation to be applied to data during encryption/decryption e.g. <code>DES</code> or
     * <code>DES/CBC/PKCS5Padding<code>.
     */
    private String transformation;

    /**
     * The <code>java.security.Provider</code> instance to use when constructing <code>Cipher</code>s.
     */
    private Provider provider;

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public Object getObject() throws Exception {
        if (singleton) {
            return cachedCipher;
        } else {
            return getCipher();
        }
    }

    public Class getObjectType() {
        return Cipher.class;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void afterPropertiesSet() throws Exception {
        if (transformation == null) {
            throw new IllegalArgumentException("Property [transformation] of class ["
                    + CipherFactoryBean.class + "] is required.");
        }

        if (singleton) {
            cachedCipher = getCipher();
        }
    }

    private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (provider == null) {
            return Cipher.getInstance(transformation);
        } else {
            return Cipher.getInstance(transformation, provider);
        }
    }
}
