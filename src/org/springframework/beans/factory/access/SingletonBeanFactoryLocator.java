/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.access;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.UrlResource;

/**
 * <p>Keyed-singleton implementation of BeanFactoryLocator, which leverages existing
 * Spring constructs. This is normally accessed through LocatorFactory, but may also
 * be used directly.</p>
 * <p>Please see the warning in BeanFactoryLocator's JavaDoc about appropriate usage
 * of singleton style BeanFactoryLocator implementations. It is the opinion of the 
 * Spring team that the use of this class and similar classes is unecessary except
 * (sometimes) for a small amount of glue code. Excessive usage will lead to code
 * that is more tightly coupled, and harder to modify or test.</p> 
 * <p>In this implementation, an ApplicationContext is built up from one or more XML
 * definition files, accessed as resources. The default name of the resource file(s)
 * is 'beanRefFactory.xml', which is used when the instance is obtained with the no-arg 
 * {@link #getInstance()} method. Using {@link #getInstance(String selector)} will
 * return a singleton instance which will use a name for the resource file(s) which
 * is the specificed selector argument, instead of the default. The purpose of this
 * Application Context is to create and hold a copy of one or more 'real' BeanFactory
 * or Application Context instances, and allow those to be obtained either directly or
 * via an alias. As such, it provides a level of indirection, and allows multiple
 * pieces of code, which are not able to work in a Dependency Injection fashion, to
 * refer to and use the same target BeanFactory/ApplicationContext instance(s), by
 * different names.<p>
 * <p>Consider an example application scenario:<br/><br/>
 * <code>com.mycompany.myapp.util.applicationContext.xml</code> - ApplicationContext
 * definition file which defines beans for 'util' layer.<br/>
 * <code>com.mycompany.myapp.dataaccess-applicationContext.xml</code> -
 * ApplicationContext definition file which defines beans for 'data access' layer.
 * Depends on the above<br/>
 * <code>com.mycompany.myapp.services.applicationContext.xml</code> -
 * ApplicationContext definition file which defines beans for 'services' layer.
 * Depends on the above<br/><br/>
 * In an ideal scenario, these would be combined to create one ApplicationContext,
 * or created as three hierarchical ApplicationContexts, by one piece of code
 * somewhere at application startup (perhaps a Servlet filter), from which all other
 * code in the application would flow, obtained as beans from the context(s). However
 * when third party code enters into the picture, things can get problematic. If the 
 * third party code needs to create user classes, which should normally be obtained
 * from a Spring BeanFactory/ApplicationContext, but can handle only newInstance()
 * style object creation, then some extra work is required to actually access and 
 * use object from a BeanFactory/ApplicationContext. One solutions is to make the
 * class created by the third party code be just a stub or proxy, which gets the
 * real object from a BeanFactory/ApplicationContext, and delegates to it. However,
 * it is is not normally workable for the stub to create the BeanFactory on each
 * use, as depending on what is inside it, that can be an expensive operation.
 * Additionally, there is a fairly tight coupling between the stub and the name of
 * the definition resource for the BeanFactory/ApplicationContext. This is where
 * KeyedSingletonBeanFactoryLocator comes in. The stub can obtain a 
 * KeyedSingletonBeanFactoryLocator instance, which is effectively a singleton, and
 * ask it for an appropriate BeanFactory. A subsequent invocation (assuming the
 * same classloader is involved) by the stub or another piece of code, will obtain
 * the same instance. The simple aliasing mechanism allows the context to be asked
 * for by a name which is appropriate for (or describes) the user. The deployer can
 * match alias names to actual context names.<br><br>
 * Another use of KeyedSingletonBeanFactoryLocator, is to demand-load/use one or more
 * BeanFactories/ApplicationContexts. Because the definiiton can contain one of more
 * BeanFactories/ApplicationContexts, which can be independent or in a hierarchy, if 
 * they are set to lazy-initialize, they will only be created when actually requested
 * for use.<br/>
 * Given the above=mentioned three ApplicationContexts, consider the simplest
 * KeyedSingletonBeanFactoryLocator usage scenario, where there is only one single
 * <code>beanRefFactory.xml</code> definition file:<br/>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
 * 
 * &lt;beans>
 * 
 *   &lt;bean id="com.mycompany.myapp"
 *         class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;list>
 *         &lt;value>com.mycompany.myapp.util.applicationContext.xml&lt;/value>
 *         &lt;value>com.mycompany.myapp.dataaccess.applicationContext.xml&lt;/value>
 *         &lt;value>com.mycompany.myapp.dataaccess.services.xml&lt;/value>
 *       &lt;/list>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * 
 * &lt;/beans>
 * </pre>
 * The client code is as simple as:
 * <pre>
 * BeanFactoryLocator bfl = KeyedSingletonBeanFactoryLocator.getInstance();
 * BeanFactoryReference bf = bfl.useFactory("com.mycompany.myapp");
 * // now use some bean from factory 
 * MyClass zed = bf.getFactory().getBean("mybean");
 * </pre>
 * Another relatively simple variation of the <code>beanRefFactory.xml</code> definition file could be:
 * <br/>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
 * 
 * &lt;beans>
 * 
 *   &lt;bean id="com.mycompany.myapp.util" lazy-init="true"
 *         class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.util.applicationContext.xml&lt;/value>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * 
 *   &lt;!-- child of above -->
 *   &lt;bean id="com.mycompany.myapp.dataaccess" lazy-init="true"
 *         class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.dataaccess.applicationContext.xml&lt;/value>
 *     &lt;/constructor-arg>
 *     &lt;constructor-arg>
 *       &lt;ref bean="com.mycompany.myapp.util"/>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * 
 *   &lt;!-- child of above -->
 *   &lt;bean id="com.mycompany.myapp.services" lazy-init="true"
 *         class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.dataaccess.services.xml&lt;/value>
 *     &lt;/constructor-arg>
 *     &lt;constructor-arg>
 *       &lt;ref bean="com.mycompany.myapp.dataaccess"/>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * 
 *   &lt;!-- define an alias -->
 *   &lt;bean id="com.mycompany.myapp.mypackage"
 *         class="java.lang.String">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.services&lt;/value>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * 
 * &lt;/beans>
 * </pre>
 * In this example, there is a hierarchy of three contexts created. The (potential)
 * advantage is that if the lazy flag is set to true, a context will only be created
 * if it's actually used. If there is some code that is only needed some of the time,
 * this mechanism can save some resources. Additionally, an alias to the last context
 * has been created. Aliases allow usage of the idiom where client code asks for a
 * context with an id which represents the package or module the code is in, and the
 * actual definition file(s) for the KeyedSingletonBeanFactoryLocator maps that id to
 * a real context id.<br/><br/>
 * A final example is more complex, with a <code>beanRefFactory.xml</code> for every module.
 * All the files are automatically combined to create the final definition.<br/>
 * <code>beanRefFactory.xml</code> file inside jar for util module:<br/><br>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
 * 
 * &lt;beans>
 *   &lt;bean id="com.mycompany.myapp.util" lazy-init="true"
 *        class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.util.applicationContext.xml&lt;/value>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * &lt;/beans>
 * </pre>
 * 
 * <code>beanRefFactory.xml</code> file inside jar for data-access module:<br/>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
 * 
 * &lt;beans>
 *   &lt;!-- child of util -->
 *   &lt;bean id="com.mycompany.myapp.dataaccess" lazy-init="true"
 *        class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.dataaccess.applicationContext.xml&lt;/value>
 *     &lt;/constructor-arg>
 *     &lt;constructor-arg>
 *       &lt;ref bean="com.mycompany.myapp.util"/>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * &lt;/beans>
 * </pre>
 * 
 * <code>beanRefFactory.xml</code> file inside jar for services module:<br/>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
 * 
 * &lt;beans>
 *   &lt;!-- child of data-access -->
 *   &lt;bean id="com.mycompany.myapp.services" lazy-init="true"
 *        class="org.springframework.context.support.ClassPathXmlApplicationContext">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.dataaccess.services.xml&lt;/value>
 *     &lt;/constructor-arg>
 *     &lt;constructor-arg>
 *       &lt;ref bean="com.mycompany.myapp.dataaccess"/>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * &lt;/beans>
 * </pre>
 * 
 * <code>beanRefFactory.xml</code> file inside jar for mypackage module. This doesn't
 * create any of its own contexts, but allows the other ones to be referred to be
 * a name known to this module:<br/>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
 * 
 * &lt;beans>
 *   &lt;!-- define an alias -->
 *   &lt;bean id="com.mycompany.myapp.mypackage"
 *         class="java.lang.String">
 *     &lt;constructor-arg>
 *       &lt;value>com.mycompany.myapp.services&lt;/value>
 *     &lt;/constructor-arg>
 *   &lt;/bean>
 * 
 * &lt;/beans>
 * </pre>
 *   
 * @author colin sampaleanu
 * @version $Revision: 1.7 $
 * @see org.springframework.context.access.LocatorFactory
 */
public class SingletonBeanFactoryLocator implements BeanFactoryLocator {

	public static final String BEANS_REFS_XML_NAME = "beanRefFactory.xml";

	protected static final Log logger = LogFactory.getLog(SingletonBeanFactoryLocator.class);

	// the keyed singleton instances
	private static Map instances = new HashMap();

	// we map BeanFactoryGroup objects by String keys, and by the definition object
	private Map bfgInstancesByKey = new HashMap();

	private Map bfgInstancesByObj = new HashMap();

	private String resourceName;

	/**
	 * Returns an instance which uses the default "beanRefFactory.xml", as the name of the
	 * definition file(s). All resources returned by the current thread's context
	 * classloader's getResources() method with this name will be combined to create a
	 * definition, which is just a BeanFactory.
	 */
	public static BeanFactoryLocator getInstance() throws FatalBeanException {
		return getInstance(BEANS_REFS_XML_NAME);
	}

	/**
	 * Returns an instance which uses the the specified selector, as the name of the
	 * definition file(s). All resources returned by the current thread's context
	 * classloader's getResources() method with this name will be combined to create a
	 * definition, which is just a a BeanFactory.
	 * @param selector the name of the resource(s) which will be read and combine to
	 * form the definition for the KeyedSingletonBeanFactoryLocator instance
	 */
	public static BeanFactoryLocator getInstance(String selector) throws FatalBeanException {
		synchronized (instances) {
			logger.debug("SingletonBeanFactoryLocator.getInstance(): SingletonBeanFactoryLocator.class="
				+ SingletonBeanFactoryLocator.class + ", hash= " + SingletonBeanFactoryLocator.class.hashCode());
			logger.debug("SingletonBeanFactoryLocator.getInstance(): instances.hashCode=" + instances.hashCode() + ", instances=" + instances);
			BeanFactoryLocator bfl = (BeanFactoryLocator) instances.get(selector);
			if (bfl == null) {
				bfl = new SingletonBeanFactoryLocator(selector);
				instances.put(selector, bfl);
			}

			return bfl;
		}
	}

	/**
	 * Constructor which uses the default "beanRefFactory.xml", as the name of the
	 * definition file(s). All resources returned by the definition classloader's
	 * getResources() method with this name will be combined to create a definition.
	 */
	protected SingletonBeanFactoryLocator() {
		resourceName = BEANS_REFS_XML_NAME;
	}

	/**
	 * Constructor which uses the the specified name as the name of the
	 * definition file(s). All resources returned by the definition classloader's
	 * getResources() method with this name will be combined to create a definition
	 * definition.
	 */
	protected SingletonBeanFactoryLocator(String resourceName) {
		this.resourceName = resourceName;
	}

	public BeanFactoryReference useFactory(String factoryKey) throws FatalBeanException {
		synchronized (bfgInstancesByKey) {
			BeanFactoryGroup bfg = (BeanFactoryGroup) bfgInstancesByKey
					.get(resourceName);
			logger.debug("bfgInstancesByKey=" + bfgInstancesByKey);

			if (bfg != null) {
				logger.debug("Factory group with resourceName '" + resourceName
						+ "' requested. Using existing instance.");
				bfg.refcount++;
			}
			else {
				// this group definition doesn't exist, we need to try to load it
				logger.debug("Factory group with resourceName '" + resourceName
						+ "' requested. Creating new instance.");

				Collection resourceUrls;
				try {
					resourceUrls = getAllDefinitionResources(resourceName);
				}
				catch (IOException e) {
					throw new FatalBeanException(
							"Unable to load group definition. Group resource name:"
									+ resourceName + ", factoryKey:" + factoryKey,
							e);
				}

				int numResources = resourceUrls.size();
				if (numResources == 0)
					throw new FatalBeanException(
							"Unable to find definition for specified definition. Group:"
									+ resourceName + ", contextId:" + factoryKey);

				String[] resources = new String[numResources];
				Iterator it = resourceUrls.iterator();
				for (int i = 0; i < numResources; ++i) {
					URL u = (URL) it.next();
					resources[i] = u.toExternalForm();
				}

				BeanFactory groupContext = createDefinition(resources);

				bfg = new BeanFactoryGroup();
				bfg.definition = groupContext;
				bfg.resourceName = resourceName;
				bfg.refcount = 1;
				bfgInstancesByKey.put(resourceName, bfg);
				bfgInstancesByObj.put(groupContext, bfg);
			}

			BeanFactory groupContext = bfg.definition;

			String lookupId = factoryKey;
			Object bean;
			try {
				bean = groupContext.getBean(lookupId);
			}
			catch (BeansException e) {
				throw new FatalBeanException(
						"Unable to return specified BeanFactory instance: factoryKey="
								+ factoryKey + ", from group with resourceName: "
								+ resourceName, e);
			}

			if (bean instanceof String) {
				// we have some indirection
				lookupId = (String) bean;
				try {
					bean = groupContext.getBean(lookupId);
				}
				catch (BeansException e) {
					throw new FatalBeanException(
							"Unable to return specified BeanFactory instance: lookupId="
									+ lookupId + ", factoryKey=" + factoryKey
									+ ", from group with resourceName: "
									+ resourceName, e);
				}
			}

			if (!(bean instanceof BeanFactory))
				throw new FatalBeanException(
						"Returned bean is not BeanFactory or its subclass. lookupId="
								+ lookupId + ", factoryKey=" + factoryKey
								+ ", from group with resourceName: " + resourceName
								+ ". Returned cbject class is: " + bean.getClass());

			final BeanFactory retval = (BeanFactory) bean;
			return new BeanFactoryReference() {
				public BeanFactory getFactory() {
					return retval;
				}
				public void release() throws FatalBeanException {
					// Currently does nothing.
					// An ideal implementation would use reference counting data to release owning
					// container when no more BeanFactories within it are used, however depending on
					// the usage scenario, this could also cause thrashing.
				}
			};
		}
	}

	/**
	 * Actually creates definition in the form of a BeanFactory, given an array of URLs
	 * representing resources which should be combined. This is split out as a separate
	 * method so that subclasses can override the actual type uses (to be an
	 * ApplicationContext, for example).
	 */
	protected BeanFactory createDefinition(String[] resources) throws FatalBeanException {
		DefaultListableBeanFactory fac = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(fac);
		for (int i = 0; i < resources.length; ++i) {
			try {
				reader.loadBeanDefinitions(new UrlResource(resources[i]));
			}
			catch (MalformedURLException e) {
				throw new FatalBeanException("Bad URL when loading definition", e);
			}
		}
		fac.preInstantiateSingletons();
		return fac;
	}

	/**
	 * method which returns resources (as URLs) which make up the definition of one
	 * beanfactory/appcontext.
	 * it is protected so that test cases may subclass this class and override this method
	 * to avoid the need for multiple classloaders to test multi-file capability in the rest
	 * of the class.
	 */
	protected Collection getAllDefinitionResources(String resourceName) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// don't depend on JDK 1.4, do our own conversion
		Enumeration e = cl.getResources(resourceName);
		ArrayList l = new ArrayList();
		while (e.hasMoreElements()) {
			l.add(e.nextElement());
		}
		return l;
	}


	// we track BeanFactory instances with this class
	private class BeanFactoryGroup {

		private String resourceName;

		private int refcount = 0;

		private BeanFactory definition;
	}

}
