package org.springframework.crypto.digest;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author Rob Harrop
 */
public abstract class AbstractSaltProviderTests extends TestCase{

    protected abstract SaltProvider getProvider() throws Exception;

    public void testCorrectSaltSize() throws Exception {
        int size = 128;
        byte[] salt = getProvider().newSalt(size);

        assertEquals("Invalid salt size.", size, salt.length);
    }

    public void testDifferentSalts() throws Exception {
        int size = 32;

        SaltProvider provider = getProvider();
        byte[] salt1 = provider.newSalt(size);
        byte[] salt2 = provider.newSalt(size);

        assertFalse("Salts should be different.", Arrays.equals(salt1, salt2));
    }
}
