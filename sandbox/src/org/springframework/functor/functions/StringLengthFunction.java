/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor.functions;

import org.springframework.functor.UnaryFunction;

/**
 * @author Keith Donald
 */
public class StringLengthFunction implements UnaryFunction {
    private static final StringLengthFunction INSTANCE = new StringLengthFunction();

    public Object execute(Object value) {
        return new Integer(String.valueOf(value).length());
    }

    public static UnaryFunction instance() {
        return INSTANCE;
    }

}