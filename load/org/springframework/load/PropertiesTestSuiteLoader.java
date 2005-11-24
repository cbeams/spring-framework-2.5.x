package org.springframework.load;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;


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
		
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties props = new Properties();
		try {
			props.load(getInputStream(file));
			
			System.out.println("Loading properties file '" + file + "'. Looking for bean definitions...");
			
			PropertiesBeanDefinitionReader bdr = new PropertiesBeanDefinitionReader(lbf);
			bdr.registerBeanDefinitions(props, null);
			
			// Get the BeanFactoryTestSuite instance
			BeanFactoryTestSuite testSuite = (BeanFactoryTestSuite) lbf.getBean("suite");
			
			// Give it access to the other beans
			testSuite.init(lbf);
			
			// Run the tests, blocking until they complete
			testSuite.runAllTests(true);
			
			// Now find all reporters
			String[] reporterNames = lbf.getBeanNamesForType(TestReporter.class);
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

	private static InputStream getInputStream(String name) throws IOException {
		try {
			return new FileInputStream(name);
		}
		catch (FileNotFoundException ex) {
			System.err.println("Failed to load from file system");
			// Try on classpath
			InputStream is = PropertiesTestSuiteLoader.class.getResourceAsStream(name);
			if (is != null) {
				return is;
			}
			else {
				throw new IOException("Cannot load properties file '" + name + "' from filesystem or classpath: " +
						" Use package name of form /com/foo/bar/MyFile.txt");
			}
		}
	}

}