package org.springframework.crypto.digest;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.security.MessageDigest;

/**
 * @author Rob Harrop
 */
public class MessageDigestFactoryBean implements FactoryBean, InitializingBean {

    private boolean singleton = true;

    private String algorithm;

    private MessageDigest cachedDigest;

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public Object getObject() throws Exception {
        if(singleton) {
            return cachedDigest;
        } else {
            return MessageDigest.getInstance(algorithm);
        }
    }

    public Class getObjectType() {
        return MessageDigest.class;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void afterPropertiesSet() throws Exception {
        if (algorithm == null) {
            throw new IllegalArgumentException("Property [algorithm] of class [" +
                    MessageDigestFactoryBean.class + "] is required.");
        }

        if(singleton) {
            cachedDigest = MessageDigest.getInstance(algorithm);
        }
    }

}
