package org.springframework.crypto.cipher;

import junit.framework.TestCase;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

/**
 * @author Rob Harrop
 */
public class CipherFactoryBeanTests extends TestCase {

    private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";

    public void testGetObjectType() {
        CipherFactoryBean bean = new CipherFactoryBean();
        assertEquals("Invalid class.", bean.getObjectType(), Cipher.class);
    }

    public void testGetSingleton() throws Exception{

        CipherFactoryBean bean = new CipherFactoryBean();
        bean.setTransformation(TRANSFORMATION);
        bean.afterPropertiesSet();
        Cipher cipher = (Cipher)bean.getObject();

        assertNotNull("Cipher is null", cipher);
        assertEquals("Invalid algorithm", TRANSFORMATION, cipher.getAlgorithm());
        System.out.println(cipher.getProvider());

    }

    public void testGetNonSingleton() throws Exception{
        CipherFactoryBean bean = new CipherFactoryBean();
        bean.setTransformation(TRANSFORMATION);
        bean.setSingleton(false);
        bean.afterPropertiesSet();
        Cipher cipher = (Cipher)bean.getObject();

        assertNotNull("Cipher is null", cipher);
        assertEquals("Invalid algorithm", TRANSFORMATION, cipher.getAlgorithm());

        Cipher cipher2 = (Cipher)bean.getObject();

        assertTrue("Cipher objects should be different", cipher2 != cipher);
    }

    public void testInvalidTransformation() throws Exception{
        CipherFactoryBean bean = new CipherFactoryBean();
        bean.setTransformation("DEF");

        try {
            bean.afterPropertiesSet();
            fail("Invalid transformation should throw NoSuchAlgorithmException");
        } catch(NoSuchAlgorithmException ex) {
            // success
        }
    }
}
