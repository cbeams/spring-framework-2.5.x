/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/AspectAccessStrategy.java,v 1.2 2004-06-12 16:32:18 kdonald Exp $
 * $Revision: 1.2 $
 * $Date: 2004-06-12 16:32:18 $
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

}