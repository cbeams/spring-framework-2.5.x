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

package org.springframework.context.support;

import java.io.Serializable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the MessageSourceResolvable interface. Easy way to
 * store all the necessary values needed to resolve messages from a
 * MessageSource.
 * 
 * @author Tony Falabella
 * @author Juergen Hoeller
 * @version $Id: DefaultMessageSourceResolvable.java,v 1.1 2004/02/13 17:42:57
 *          jhoeller Exp $
 */
public class DefaultMessageSourceResolvable implements MessageSourceResolvable,
        Serializable {

    private String[] codes;

    private Object[] arguments;

    private String defaultMessage;

    /**
     * Create a new instance, using multiple codes.
     * 
     * @see MessageSourceResolvable#getCodes
     */
    public DefaultMessageSourceResolvable(String[] codes, Object[] arguments) {
        this(codes, arguments, null);
    }

    /**
     * Create a new instance, using multiple codes and a default message.
     * 
     * @see MessageSourceResolvable#getCodes
     */
    public DefaultMessageSourceResolvable(String[] codes, Object[] arguments,
            String defaultMessage) {
        this.codes = codes;
        this.arguments = arguments;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Copy constructor: Create a new instance from another resolvable.
     */
    public DefaultMessageSourceResolvable(MessageSourceResolvable resolvable) {
        this(resolvable.getCodes(), resolvable.getArguments(), resolvable
                .getDefaultMessage());
    }

    /**
     * Used only by subclasses who need to do custom initialization before
     * setting code, arguments, and/or the default message.
     */
    protected DefaultMessageSourceResolvable() {

    }

    public String[] getCodes() {
        return codes;
    }

    /**
     * Return the default code of this resolvable, i.e. the last one in the
     * codes array.
     */
    public String getCode() {
        return (codes != null && codes.length > 0) ? codes[codes.length - 1]
                : null;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    protected void setCodes(String[] codes) {
        this.codes = (String[])codes.clone();
    }

    protected void setArguments(Object[] arguments) {
        this.arguments = (Object[])arguments.clone();
    }

    protected void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    protected String resolvableToString() {
        StringBuffer msgBuff = new StringBuffer();
        msgBuff.append("codes=["
                + StringUtils.arrayToDelimitedString(getCodes(), ",")
                + "]; arguments=[");
        if (arguments == null) {
            msgBuff.append("null");
        }
        else {
            for (int i = 0; i < getArguments().length; i++) {
                msgBuff.append("(" + getArguments()[i].getClass().getName()
                        + ")[" + getArguments()[i] + "]");
                if (i < getArguments().length - 1)
                    msgBuff.append(", ");
            }
        }
        msgBuff.append("]; defaultMessage=[" + getDefaultMessage() + "]");
        return msgBuff.toString();
    }

    public String toString() {
        return resolvableToString();
    }

}
