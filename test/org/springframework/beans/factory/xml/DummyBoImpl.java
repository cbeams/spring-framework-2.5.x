/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DummyBoImpl.java,v 1.1 2003-11-22 15:53:50 johnsonr Exp $
 */
public class DummyBoImpl implements DummyBo {
	
	DummyDao dao;

	public DummyBoImpl(DummyDao dao) {
		this.dao = dao;
	}
	
	public void something() {
		
	}

}
