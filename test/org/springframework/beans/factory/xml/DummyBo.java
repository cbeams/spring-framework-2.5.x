/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DummyBo.java,v 1.1 2003-11-22 15:08:28 johnsonr Exp $
 */
public class DummyBo {
	
	DummyDao dao;

	public DummyBo(DummyDao dao) {
		this.dao = dao;
		
	}

}
