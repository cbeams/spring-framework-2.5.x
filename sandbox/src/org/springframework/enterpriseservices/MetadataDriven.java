/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices;

import org.springframework.metadata.Attributes;

/**
 * 
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: MetadataDriven.java,v 1.1 2003-11-22 09:05:39 johnsonr Exp $
 */
public interface MetadataDriven {
	
	void setAttributes(Attributes attributes);

}
