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
package org.springframework.web.servlet.view.jasperreports;

import org.springframework.core.NestedRuntimeException;

/**
 * Extension thrown when the file extension of a report file is unrecognized.
 * Only .jrxml and .jasper file extensions are valid.
 * 
 * @author robh
 */
public class UnrecognizedReportExtensionException extends
        NestedRuntimeException {

    /**
     * @param message
     */
    public UnrecognizedReportExtensionException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public UnrecognizedReportExtensionException(String message, Throwable cause) {
        super(message, cause);
    }
}