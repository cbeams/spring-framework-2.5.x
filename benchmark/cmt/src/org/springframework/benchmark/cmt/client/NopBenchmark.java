/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.benchmark.cmt.server.Benchmark;


/**
 * 
 * @author Rod Johnson
 * @version $Id: NopBenchmark.java,v 1.1 2003-12-02 18:31:08 johnsonr Exp $
 */
public class NopBenchmark extends AbstractBenchmark {
	
	private int invocations;
	
	public void setInvocations(int invocations) {
		this.invocations = invocations;
	}

	protected void runPass(int n) throws Exception {
		Benchmark bm = factory.getBenchmark();
		for (int i = 0; i < invocations; i++) {
			bm.doVeryLittle();
		}
	}
	
}
