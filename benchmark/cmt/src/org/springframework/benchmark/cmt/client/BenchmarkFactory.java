/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * 
 * @author Rod Johnson
 * @version $Id: BenchmarkFactory.java,v 1.1 2003-12-02 18:31:08 johnsonr Exp $
 */
public interface BenchmarkFactory {
	
	Benchmark getBenchmark() throws Exception;

}
