/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * 
 * @author Rod Johnson
 */
public interface BenchmarkHome extends EJBHome {
	
	BenchmarkRemote create() throws RemoteException, CreateException;

}
