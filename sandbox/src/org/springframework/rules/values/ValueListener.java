/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValueListener.java,v 1.1 2004-06-12 07:27:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * Simple listener interface for clients interested in being notified of a value
 * change.
 * 
 * @author Keith Donald
 */
public interface ValueListener {
    public void valueChanged();
}