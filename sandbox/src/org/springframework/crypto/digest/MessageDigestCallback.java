package org.springframework.crypto.digest;

import java.security.MessageDigest;

/**
 * @author Rob Harrop
 */
public interface MessageDigestCallback {
    Object doWithMessageDigest(MessageDigest messageDigest);
}
