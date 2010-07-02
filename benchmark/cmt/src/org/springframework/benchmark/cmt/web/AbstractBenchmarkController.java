/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import org.springframework.benchmark.cmt.client.BenchmarkFactory;
import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * 
 * @author Rod Johnson
 */
public abstract class AbstractBenchmarkController extends AbstractController {
	
	Log log = LogFactory.getLog(getClass());
	
	// Could allow override
	public final static int USERS = 99; // indexed from 1 to 100
	
	public final static int ITEMS = 999;
	
	private Random rand = new Random();
	
	/** Map from name to benchmarkfactory */
	private HashMap benchmarkFactories = new HashMap();
	
	private Config config;
	
	private String [] benchmarkNames;
	
	public void setConfig(Config config) {
		this.config = config;
	}
	
	/** 
	 * Convenience method for subclasses
	 * @param sz size of array to index
	 * @return a random array of list index from 0 up to sz-1
	 */
	protected int randomIndex(int sz) {
		return Math.abs(rand.nextInt(sz));
	}

	
	private Benchmark getBenchmark(String name) throws Exception {
		BenchmarkFactory bf = (BenchmarkFactory) benchmarkFactories.get(name);
		if (bf == null)
			throw new ServletException("No benchmark factory with name '" + name + "': " +
		"valid values are " + StringUtils.arrayToCommaDelimitedString(this.benchmarkNames));
		return bf.getBenchmark();
	} 
	
	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected final ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		//String name = request.getParameter("bm");
		//if (name == null)
		//	throw new ServletException("'bm' parameter may not be null: " +
		//			"valid values are " + StringUtils.arrayToCommaDelimitedString(this.benchmarkNames));
		
		String name = config.getMode();
		
		// Could add timing to model
		Benchmark benchmark;
		try {
			long st = System.currentTimeMillis();
			benchmark = getBenchmark(name);
			ModelAndView mv = run(benchmark);
			long et =  System.currentTimeMillis() - st;
			mv.getModel().put("time", "" + et);
			return mv;
		}
		catch (Exception ex) {
			throw new ServletException(ex);
		}
	
	}
	
	protected abstract ModelAndView run(Benchmark benchmark) throws Exception;


	/**
	 * @see org.springframework.context.support.ApplicationObjectSupport#initApplicationContext()
	 */
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		
		String[] names = getApplicationContext().getBeanNamesForType(BenchmarkFactory.class);
		for (int i = 0; i < names.length; i++) {
			BenchmarkFactory bmf = (BenchmarkFactory) getApplicationContext().getBean(names[i]);
			this.benchmarkFactories.put(names[i], bmf);
			logger.info("Mapping from " + names[i] + " to " + bmf);
		}
		this.benchmarkNames = names;
	}
}
