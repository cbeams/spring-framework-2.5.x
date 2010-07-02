/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.benchmark.cmt.server.dao.BenchmarkDao;
import org.springframework.benchmark.cmt.server.dao.JdbcBenchmarkDao;
import org.springframework.benchmark.cmt.server.pojo.PojoBenchmark;

/**
 * 
 * @author Rod Johnson
 */
public class BenchmarkEjb extends PojoBenchmark implements SessionBean {

	/**
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sc) throws EJBException, RemoteException {
		// Look up dataSource
		
		// Do people still write this rubbish?
		try {
			Context ctx = new InitialContext();
		
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/oracle");
			
			System.out.println("EJB: DataSource is " + ds);
		
			// Create DAO
			BenchmarkDao dao = new JdbcBenchmarkDao(ds);
			
			setDao(dao);
		}
		catch (NamingException ex ) {
			throw new EJBException("Can't look up datasource: " + ex);
		}
		
	}
	
	public void ejbCreate() {
		
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
		throw new UnsupportedOperationException();
	}

}
