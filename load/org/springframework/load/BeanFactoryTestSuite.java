package org.springframework.load;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;



/**
 * A test suite defined as a bean in a ListableBeanFactory.
 * The bean factory will also manage reporters and any test fixture.
 * @author Rod Johnson
 */
public class BeanFactoryTestSuite extends AbstractTestSuite implements InitializingBean {
	
	/** List of String bean name */
	private List testNames;
	
	private BeanFactory beanFactory;
	
	/**
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (getPasses() == 0)
			throw new Exception("Must set passes property");
		if (getThreads() == 0)
			throw new Exception("Must set threads property");
		if (getReportInterval() == 0)
			throw new Exception("Must set reportIntervalSeconds or reportInterval (ms) property");

		//if (getMaxPause() <= 0)
		//	throw new Exception("Must set maxPause property");
	}
	
	public void init(ListableBeanFactory lbf) {
		System.out.println("init");
		this.beanFactory = lbf;
		String[] allTestNames = lbf.getBeanNamesForType(Test.class);
		this.testNames = new LinkedList();
		if (allTestNames.length == 0)
			throw new RuntimeException("No test beans defined");
		for (int i = 0; i < allTestNames.length; i++) {
			// TODO hack need abstract bean functionality
			if (!allTestNames[i].startsWith("_")) {
				this.testNames.add(allTestNames[i]);
			}
			else {
				System.out.println("Bean '" + allTestNames[i] + "' is not a running test");
			}
		}
			
//		try {
//			Object fixture = lbf.getBean("fixture");
//			System.out.println("Set fixture");
//			setFixture(fixture);
//		}
//		catch (NoSuchBeanDefinitionException ex) {
//			System.out.println("No fixture found");
//		}
	}

	/**
	 * @see AbstractTestSuite#createTests()
	 */
	protected Test[] createTests() {
		System.out.println("Number of worker threads = " + getThreads());
		Test[] tests = new Test[getThreads()];
		int index = 0;
		for (int i = 0; i < getThreads(); ) {
			String name = (String) testNames.get(index);
			Test test = (Test) this.beanFactory.getBean(name);
			for (int j = 0; j < test.getInstances() && i < getThreads(); j++) {
				tests[i] = (Test) this.beanFactory.getBean(name);
				//System.out.println("New test " + tests[j].getClass());
				++i;
			}
			
			++index;
			if (index == testNames.size()) {
				// Wrap
				index = 0;
			}
		}
		return tests;
	}

}