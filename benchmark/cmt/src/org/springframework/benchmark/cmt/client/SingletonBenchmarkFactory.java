/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * 
 * @author Rod Johnson
 */
public class SingletonBenchmarkFactory implements BenchmarkFactory {
	
	private Benchmark benchmark;
	
	public SingletonBenchmarkFactory(Benchmark benchmark) {
		this.benchmark = benchmark;
	}

	/**
	 * @see org.springframework.benchmark.cmt.client.BenchmarkFactory#getBenchmark()
	 */
	public Benchmark getBenchmark() throws Exception {
	//	System.out.println("Singleton factory");
		return benchmark;
	}

}
