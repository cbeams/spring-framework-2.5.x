/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/MetaAspectAccessStrategy.java,v 1.1 2004-06-14 22:39:02 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-14 22:39:02 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public interface MetaAspectAccessStrategy {
    public boolean isReadable(String aspect);
    public boolean isWriteable(String aspect);
    public boolean isEnumeration(String aspect);
    public Class getAspectClass(String aspect);
}
