package org.springframework.crypto.digest;

import junit.framework.TestCase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Rob Harrop
 */
public class MessageDigestFactoryBeanTests extends TestCase {
    private static final String ALGORITHM = "SHA";

    public void testGetObjectType() {
        MessageDigestFactoryBean bean = new MessageDigestFactoryBean();
        assertEquals("Invalid class.", bean.getObjectType(), MessageDigest.class);
    }

    public void testGetSingleton() throws Exception {

        MessageDigestFactoryBean bean = new MessageDigestFactoryBean();
        bean.setAlgorithm(ALGORITHM);
        bean.afterPropertiesSet();
        MessageDigest digest = (MessageDigest) bean.getObject();

        assertNotNull("Digest is null", digest);
        assertEquals("Invalid algorithm", ALGORITHM, digest.getAlgorithm());

    }

    public void testGetNonSingleton() throws Exception {
        MessageDigestFactoryBean bean = new MessageDigestFactoryBean();
        bean.setAlgorithm(ALGORITHM);
        bean.setSingleton(false);
        bean.afterPropertiesSet();
        MessageDigest digest = (MessageDigest) bean.getObject();

        assertNotNull("Digest is null", digest);
        assertEquals("Invalid algorithm", ALGORITHM, digest.getAlgorithm());

        MessageDigest digest2 = (MessageDigest) bean.getObject();

        assertTrue("MessageDigest objects should be different", digest != digest2);
    }

    public void testInvalidTransformation() throws Exception {
        MessageDigestFactoryBean bean = new MessageDigestFactoryBean();
        bean.setAlgorithm("DEF");

        try {
            bean.afterPropertiesSet();
            fail("Invalid algorithm should throw NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException ex) {
            // success
        }
    }
}
