package org.springframework.load;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;


/**
 * Main class for the load test package.
 * Loads beans from a BeanFactory
 */
public class PropertiesTestSuiteLoader  {
	
	public static void main(String[] args) {
		String file = null;
		
		if (args.length < 1) {
			System.out.println("Usage: PropertiesTestSuiteLoader <properties file>");
			System.exit(1);
			
		}
		file = args[0];
		
		ListableBeanFactoryImpl lbf = new ListableBeanFactoryImpl();
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
			
			System.out.println("Loading properties file '" + file + "'. Looking for bean definitions...");
			
			lbf.registerBeanDefinitions(props, null);
			
			// Get the BeanFactoryTestSuite instance
			BeanFactoryTestSuite testSuite = (BeanFactoryTestSuite) lbf.getBean("suite");
			
			// Give it access to the other beans
			testSuite.init(lbf);
			
			// Run the tests, blocking until they complete
			testSuite.runAllTests(true);
			
			// Now find all reporters
			String[] reporterNames = lbf.getBeanDefinitionNames(TestReporter.class);
			TestReporter[] reporters = new TestReporter[reporterNames.length];
			for (int i = 0; i < reporterNames.length; i++) {
				System.out.println("Found reporter name " + reporterNames[i]);
			}
			
			System.out.println("Found " + reporters.length + " reporter beans");
			
			//XmlTestReporter reporter = new XmlTestReporter();
			//reporter.setXmlReportFileName("c:\\scratch\\loadTests.xml");
			
			for (int i = 0 ; i < reporters.length; i++) {
				TestReporter reporter = (TestReporter) lbf.getBean(reporterNames[i]);
				reporter.generateReport(testSuite);
			}
			
			
		}
		catch (IOException ex) {
			System.out.println("Cannot find properties file: " + ex);
		}
		catch (BeansException ex) {
			System.out.println("Cannot find bean: " + ex);
			ex.printStackTrace();
		}
		catch (Exception ex) {
			// Reporter failed...?
			ex.printStackTrace();
		}
	}

}