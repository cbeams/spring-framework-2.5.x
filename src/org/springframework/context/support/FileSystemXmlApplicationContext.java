package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.AbstractXmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;

/**
 * Standalone XML application context, taking the context definition files
 * from the file system or from URLs. Mainly useful for test harnesses,
 * but also for standalone environments.
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class FileSystemXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML file.
	 * @param configLocation file path
	 */
	public FileSystemXmlApplicationContext(String configLocation) throws BeansException {
		this.configLocations = new String[] {configLocation};
		refresh();
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 */
	public FileSystemXmlApplicationContext(String[] configLocations) throws BeansException {
		this.configLocations = configLocations;
		refresh();
	}

	/**
	 * Create a new FileSystemXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 */
	public FileSystemXmlApplicationContext(String[] configLocations, ApplicationContext parent)
			throws BeansException {
		super(parent);
		this.configLocations = configLocations;
		refresh();
	}

	protected void loadBeanDefinitions(AbstractXmlBeanDefinitionReader reader) throws BeansException, IOException {
		if (this.configLocations != null) {
			for (int i = 0; i < this.configLocations.length; i++) {
				reader.loadBeanDefinitions(getResourceAsStream(this.configLocations[i]));
			}
		}
	}

}
