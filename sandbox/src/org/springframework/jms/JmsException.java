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
 
package org.springframework.jms;

/**
 * Exception thrown by the framework whenever it encounters a problem related to JMS.
 *
 * @author Andre Biryukov
 * @author <a href="mailto:les@hazlewood.com">Les Hazlewood</a>
 */
public class JmsException extends org.springframework.core.NestedRuntimeException {


    /**
     * Constructor for JmsException.
     *
     * @param s Custom message string
     * @param ex Original exception
     */
    public JmsException(final String s, final Throwable ex) {
        super(s, ex);
    }
    

    /**
     * Constructor for JmsException
     *
     * @param s Custom message string.
     */
    public JmsException(final String s) {
        super(s);
    }
    
    /**
     * Constructor that allows a nested exception.  This is primarily meant to
     * be a convenience constructor for subclasses mirroring respective
     * javax.jms Exceptions.
     * 
     * @param cause the cause of the exception.  This argument is generally
     * expected to be a proper subclass of {@link javax.jms.JMSException}
     */
    public JmsException( final Throwable cause ) {
        this( null, cause );
    }



}
