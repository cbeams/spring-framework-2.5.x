/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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