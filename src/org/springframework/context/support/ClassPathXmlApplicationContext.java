package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Standalone XML application context, taking the context definition
 * files from the class path. Mainly useful for test harnesses,
 * but also for application contexts embedded within JARs.
 *
 * <p>Note: Generally treats (file) paths as class path resources, when using
 * ApplicationContext.getResource. Only supports full classpath resource
 * names that include the package path, like "mypackage/myresource.dat".
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 */
public class ClassPathXmlApplicationContext extends FileSystemXmlApplicationContext {

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML file.
	 * @param configLocation file path
	 */
	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		super(configLocation);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 */
	public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
		super(configLocations);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent)
	    throws BeansException {
		super(configLocations, parent);
	}

	/**
	 * This implementation treats paths as class path resources.
	 * Only supports full class path names including package specification,
	 * like "/mypackage/myresource.dat". A root slash gets prepended to
	 * the path if not already contained.
	 */
	protected Resource getResourceByPath(String path) throws IOException {
		return new ClassPathResource(path);
	}

}
