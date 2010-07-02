package org.springframework.crypto.digest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * @author Rob Harrop
 */
public class MessageDigestTemplate implements InitializingBean {

    private static final Log log = LogFactory.getLog(MessageDigestTemplate.class);

    private MessageDigest messageDigest;

    private boolean cloneable;

    public void setMessageDigest(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    public Object execute(MessageDigestCallback callback) {

        if (cloneable) {
            try {
                return executeWithClone(callback);
            } catch (CloneNotSupportedException ex) {
                log.warn("CloneNotSupportedException thrown for MessageDigest that supports clone(). " +
                        "Executing under synchronization instead.", ex);
                return executeSynchronized(callback);
            }
        } else {
            return executeSynchronized(callback);
        }
    }

    protected Object executeWithClone(MessageDigestCallback callback) throws CloneNotSupportedException {
        MessageDigest md = (MessageDigest) messageDigest.clone();
        return callback.doWithMessageDigest(md);
    }

    protected Object executeSynchronized(MessageDigestCallback callback) {
        synchronized (messageDigest) {
            messageDigest.reset();
            return callback.doWithMessageDigest(messageDigest);
        }
    }

    public byte[] digest(final String data) {
        return digest(data.getBytes());
    }

    public byte[] digest(final String data, final String encoding) {
        try {
            return digest(data.getBytes(encoding));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Invalid encoding [" + encoding + "].");
        }
    }

    public byte[] digest(final byte[] data) {
        return (byte[])execute(new MessageDigestCallback() {
            public Object doWithMessageDigest(MessageDigest messageDigest) {
                messageDigest.update(data);
                return messageDigest.digest();
            }
        });
    }

    public byte[] digest(final byte[] data, final byte[] salt) {
        return (byte[])execute(new MessageDigestCallback() {
            public Object doWithMessageDigest(MessageDigest messageDigest) {
                messageDigest.update(salt);
                messageDigest.update(data);
                return messageDigest.digest();
            }
        });
    }

    public void afterPropertiesSet() throws Exception {
        if (messageDigest == null) {
            throw new IllegalArgumentException("Property [messageDigest] of class [" +
                    MessageDigestTemplate.class + "] is required.");
        }

        this.cloneable = isCloneable(messageDigest);
    }

    private boolean isCloneable(MessageDigest messageDigest) {
        try {
            messageDigest.clone();
            return true;
        } catch (CloneNotSupportedException ex) {
            return false;
        }
    }
}
