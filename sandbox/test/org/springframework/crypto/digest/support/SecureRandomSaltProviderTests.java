package org.springframework.crypto.digest.support;

import org.springframework.crypto.digest.AbstractSaltProviderTests;
import org.springframework.crypto.digest.SaltProvider;

/**
 * @author Rob Harrop
 */
public class SecureRandomSaltProviderTests extends AbstractSaltProviderTests {

    protected SaltProvider getProvider() throws Exception {
        SecureRandomSaltProvider provider = new SecureRandomSaltProvider();
        provider.afterPropertiesSet();
        return provider;
    }
}
