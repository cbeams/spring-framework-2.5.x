/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/ValidationResults.java,v 1.10 2004-04-18 03:34:53 kdonald Exp $
 * $Revision: 1.10 $
 * $Date: 2004-04-18 03:34:53 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.rules;

import java.util.Map;

/**
 * %single sentence summary caption%.
 * 
 * %long description%.
 *
 * @author  Keith Donald
 */
public interface ValidationResults {
    public Map getResults();
    public UnaryPredicate getResults(String propertyName);
}