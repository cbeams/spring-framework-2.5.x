/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValueChangeable.java,v 1.1 2004-06-12 07:27:08 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:08 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * Client interface for value model objects whose value can change.
 * 
 * @author Keith Donald
 */
public interface ValueChangeable {
    public void addValueListener(ValueListener l);

    public void removeValueListener(ValueListener l);
}