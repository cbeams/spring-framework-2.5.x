/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.benchmark.cmt.server.Benchmark;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rod Johnson
 * @version $Id: XmlBenchmarkFactory.java,v 1.3 2003-12-30 13:52:39 jhoeller Exp $
 */
public class XmlBenchmarkFactory implements BenchmarkFactory {
	
	private String file;
	
	private Benchmark benchmark;

	public void setFile(String file) {
		this.file = file;
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource(file, getClass()));
		this.benchmark = (Benchmark) bf.getBean("benchmark");
	} 

	public Benchmark getBenchmark() {
		return benchmark;
	}
	
	public String toString() {
		return "XmlBenchmarkFactory: file='" + file + "'";
	}

}
