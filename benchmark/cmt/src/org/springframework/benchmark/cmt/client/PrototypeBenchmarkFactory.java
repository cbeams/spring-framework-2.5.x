/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import org.springframework.benchmark.cmt.server.Benchmark;
import org.springframework.benchmark.cmt.server.dao.BenchmarkDao;
import org.springframework.benchmark.cmt.server.pojo.PojoBenchmark;

/**
 * 
 * @author Rod Johnson
 */
public class PrototypeBenchmarkFactory implements BenchmarkFactory, BeanFactoryAware {
	
	private BeanFactory beanFactory;
	
	private BenchmarkDao dao;
	
	public PrototypeBenchmarkFactory(BenchmarkDao dao) {
		this.dao = dao;
	}

	/**
	 * @see org.springframework.benchmark.cmt.client.BenchmarkFactory#getBenchmark()
	 */
	public Benchmark getBenchmark() throws Exception {
	//	System.out.println("Singleton factory");
	//	return (Benchmark) beanFactory.getBean(name);
		
		// Minimal overhead
		return new PojoBenchmark(dao);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		
		this.dao = (BenchmarkDao) beanFactory.getBean("dao");
	}

}
