/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.springframework.benchmark.cmt.server.Benchmark;
import org.springframework.benchmark.cmt.server.ejb.BenchmarkHome;

/**
 * 
 * @author Rod Johnson
 */
public class EjbBenchmarkFactory implements BenchmarkFactory {
	
	private BenchmarkHome home;
	
	public EjbBenchmarkFactory() throws Exception {
		Context ctx = new InitialContext();
		home = (BenchmarkHome) ctx.lookup("benchmark");
	}

	/**
	 * @see org.springframework.benchmark.cmt.client.BenchmarkCreator#getBenchmark()
	 */
	public Benchmark getBenchmark() throws Exception {
		//System.out.println("EJB FACTORY");
		return home.create();
	}

}
