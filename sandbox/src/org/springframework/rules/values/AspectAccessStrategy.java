/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/AspectAccessStrategy.java,v 1.3 2004-06-13 11:35:31 kdonald Exp $
 * $Revision: 1.3 $
 * $Date: 2004-06-13 11:35:31 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * 
 * @author Keith Donald
 */
public interface AspectAccessStrategy {
    public Object getValue(String aspect);
    public Object getDomainObject();
}