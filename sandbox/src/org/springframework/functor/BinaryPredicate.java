/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/functor/BinaryPredicate.java,v 1.1 2004-03-30 00:32:19 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-03-30 00:32:19 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

import java.io.Serializable;

/**
 * @author Keith Donald
 */
public interface BinaryPredicate extends Serializable {
    public boolean evaluate(Object value1, Object value2);
}
