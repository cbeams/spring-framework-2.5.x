/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/AspectAccessStrategy.java,v 1.1 2004-06-12 07:27:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public interface AspectAccessStrategy {
    public void addValueListener(ValueListener o, String aspect);

    public void removeValueListener(ValueListener o, String aspect);

    public Object getValue(String aspect);

    public void setValue(String aspect, Object value);
}