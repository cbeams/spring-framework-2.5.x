/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.easymock.MockControl;

import org.springframework.benchmark.cmt.data.User;
import org.springframework.benchmark.cmt.server.dao.BenchmarkDao;
import org.springframework.benchmark.cmt.server.pojo.PojoBenchmark;

/**
 * 
 * @author Rod Johnson
 */
public class BenchmarkTests extends TestCase {
	
	//private Benchmark benchmark;
	
	public BenchmarkTests(String s) {
		super(s);
	}
	
	protected void setUp() {
		//benchmark = new PojoBenchmark();
	}
	
	public void testCantGetNonexistentUser() throws RemoteException {
		long id = 1;
		MockControl mc = MockControl.createControl(BenchmarkDao.class);
		BenchmarkDao dao = (BenchmarkDao) mc.getMock();
		dao.getUser(id);
		mc.setReturnValue(null);
		mc.replay();
		
		Benchmark bm = getBenchmark(dao);
		try {
			bm.getUser(id);
			fail();
		}
		catch (NoSuchUserException ex) {
			// Ok
			assertEquals(id, ex.getId());
		}
		mc.verify();
	}
	
	public void testGetValidUser() throws NoSuchUserException, RemoteException {
		long id = 1;
		User user = new User("rod", "johnson");
		MockControl mc = MockControl.createControl(BenchmarkDao.class);
		BenchmarkDao dao = (BenchmarkDao) mc.getMock();
		dao.getUser(id);
		mc.setReturnValue(user);
		mc.replay();
	
		Benchmark bm = getBenchmark(dao);
		User u = bm.getUser(id);
		assertTrue(u == user);
			
		mc.verify();
	}


	protected Benchmark getBenchmark(BenchmarkDao dao) {
		return new PojoBenchmark(dao);
	}

}
