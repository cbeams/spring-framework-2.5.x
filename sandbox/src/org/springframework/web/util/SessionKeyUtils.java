/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author Keith Donald
 */
public class SessionKeyUtils {
    public static String generateMD5SessionKey(String input, boolean asBase64) {
        if (!asBase64) {
            return DigestUtils.md5Hex(input);
        }
        byte[] encoded = Base64.encodeBase64(DigestUtils.md5(input));
        return new String(encoded);
    }
}