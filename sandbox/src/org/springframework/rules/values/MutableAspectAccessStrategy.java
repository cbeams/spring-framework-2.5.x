/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/MutableAspectAccessStrategy.java,v 1.2 2004-06-13 11:35:31 kdonald Exp $
 * $Revision: 1.2 $
 * $Date: 2004-06-13 11:35:31 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public interface MutableAspectAccessStrategy extends AspectAccessStrategy {
    public void addValueListener(ValueListener o, String aspect);

    public void removeValueListener(ValueListener o, String aspect);

    public void setValue(String aspect, Object value);
}