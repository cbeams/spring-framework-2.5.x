/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/CommitListener.java,v 1.1 2004-08-03 22:03:33 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-08-03 22:03:33 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

public interface CommitListener {
    public boolean preEditCommitted(Object formObject);

    public void postEditCommitted(Object formObject);
}