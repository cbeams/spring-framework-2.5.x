/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/functor/UnaryFunction.java,v 1.1 2004-03-30 00:32:19 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-03-30 00:32:19 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

/**
 * @author Keith Donald
 */
public interface UnaryFunction {
    public Object execute(Object value);
}
