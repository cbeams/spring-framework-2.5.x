/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/MutableAspectAccessStrategy.java,v 1.1 2004-06-12 16:32:18 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 16:32:18 $
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

    public Object getDomainObject();
    
}