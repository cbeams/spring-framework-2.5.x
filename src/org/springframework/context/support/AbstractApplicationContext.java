/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventMulticaster;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ContextOptions;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NestingMessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.config.ApplicationContextAwareProcessor;
import org.springframework.context.config.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticasterImpl;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;


/**
 * Partial implementation of ApplicationContext. Doesn't mandate the
 * type of storage used for configuration, but implements common functionality.
 *
 * <p>This class uses the Template Method design pattern, requiring
 * concrete subclasses to implement protected abstract methods.
 *
 * <p>The context options may be supplied as a bean in the default bean factory,
 * with the name "contextOptions".
 *
 * <p>A message source may be supplied as a bean in the default bean factory,
 * with the name "messageSource". Else, message resolution is delegated to the
 * parent context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 21, 2001
 * @version $Revision: 1.16 $
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see #OPTIONS_BEAN_NAME
 * @see #MESSAGE_SOURCE_BEAN_NAME
 */
public abstract class AbstractApplicationContext implements ConfigurableApplicationContext {

	/**
	 * Name of options bean in the factory.
	 * If none is supplied, a default ContextOptions instance will be used.
	 * @see ContextOptions
	 */
	public static final String OPTIONS_BEAN_NAME = "contextOptions";

	/**
	 * Name of the MessageSource bean in the factory.
	 * If none is supplied, message resolution is delegated to the parent.
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Log4j logger used by this class. Available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent context */
	private ApplicationContext parent;

	/** Display name */
	private String displayName = getClass().getName() + ";hashCode=" + hashCode();

	/** System time in milliseconds when this context started */
	private long startupTime;

	/** Special bean to handle configuration */
	private ContextOptions contextOptions;

	/** MessageSource helper we delegate our implementation of this interface to */
	private MessageSource messageSource;

	/**
	 * Helper class used in event publishing.
	 * TODO: This could be parameterized as a JavaBean (with a distinguished name
	 * specified), enabling a different thread usage policy for event publication.
	 */
	private ApplicationEventMulticaster eventMulticaster = new ApplicationEventMulticasterImpl();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent parent context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this.parent = parent;
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext
	//---------------------------------------------------------------------

	/**
	 * Subclasses may call this to set parent after constructor.
	 * Note that parent shouldn't be changed: it should only be
	 * set later if it isn't available when an object of this
	 * class is created.
	 * @param ac parent context
	 */
	protected void setParent(ApplicationContext ac) {
		this.parent = ac;
	}

	/**
	 * Return the parent context, or null if there is no parent,
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or null if there is no parent
	 */
	public ApplicationContext getParent() {
		return parent;
	}

	/**
	 * To avoid endless constructor chaining, only concrete classes
	 * take this in their constructor, and then invoke this method
	 */
	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Return a friendly name for context
	 * @return a display name for the context
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Return the timestamp when this context was first loaded
	 * @return the timestamp (ms) when this context was first loaded
	 */
	public final long getStartupDate() {
		return startupTime;
	}

	/**
	 * Return context options. These control reloading etc.
	 * @return context options
	 */
	public final ContextOptions getOptions() {
		return this.contextOptions;
	}

	/**
	 * Load or reload configuration.
	 * @throws ApplicationContextException if the configuration was invalid or couldn't
	 * be found, or if configuration has already been loaded and reloading is forbidden
	 * @throws BeansException if the bean factory could not be initialized
	 */
	public final void refresh() throws BeansException {
		if (this.contextOptions != null && !this.contextOptions.isReloadable()) {
			throw new ApplicationContextException("Forbidden to reload config");
		}

		this.startupTime = System.currentTimeMillis();

		refreshBeanFactory();
		getBeanFactory().ignoreDependencyType(ApplicationContext.class);
		getBeanFactory().addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

		if (getBeanDefinitionCount() == 0) {
			logger.warn("No beans defined in ApplicationContext [" + getDisplayName() + "]");
		}
		else {
			logger.info(getBeanDefinitionCount() + " beans defined in ApplicationContext [" + getDisplayName() + "]");
		}

		// invoke factory processors that can override values in the bean definitions
		invokeBeanFactoryPostProcessors();

		// register bean processor that intercept bean creation
		registerBeanPostProcessors();

		// load options bean for this context
		loadOptions();

		// initialize message source for this context
		initMessageSource();

		// initialize other special beans in specific context subclasses
		onRefresh();

		// check for listener beans and register them
		refreshListeners();

		// instantiate singletons this late to allow them to access the message source
		getBeanFactory().preInstantiateSingletons();

		// last step: publish respective event
		publishEvent(new ContextRefreshedEvent(this));
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * Must be called before singleton instantiation.
	 */
	private void invokeBeanFactoryPostProcessors() throws BeansException {
		String[] beanNames = getBeanDefinitionNames(BeanFactoryPostProcessor.class);
		BeanFactoryPostProcessor[] factoryProcessors = new BeanFactoryPostProcessor[beanNames.length];
		for (int i = 0; i < beanNames.length; i++) {
			factoryProcessors[i] = (BeanFactoryPostProcessor) getBean(beanNames[i]);
		}
		Arrays.sort(factoryProcessors, new OrderComparator());
		for (int i = 0; i < factoryProcessors.length; i++) {
			BeanFactoryPostProcessor factoryProcessor = factoryProcessors[i];
			factoryProcessor.postProcessBeanFactory(getBeanFactory());
		}
	}

	/**
	 * Instantiate and invoke all registered BeanPostProcessor beans,
	 * respecting explicit order if given.
	 * Must be called before singleton instantiation.
	 */
	private void registerBeanPostProcessors() throws BeansException {
		String[] beanNames = getBeanDefinitionNames(BeanPostProcessor.class);
		if (beanNames.length > 0) {
			List beanProcessors = new ArrayList();
			for (int i = 0; i < beanNames.length; i++) {
				beanProcessors.add(getBean(beanNames[i]));
			}
			Collections.sort(beanProcessors, new OrderComparator());
			for (Iterator it = beanProcessors.iterator(); it.hasNext();) {
				getBeanFactory().addBeanPostProcessor((BeanPostProcessor) it.next());
			}
		}
	}

	/**
	 * Load the options bean.
	 * The BeanFactory must be loaded before this method is called.
	 */
	private void loadOptions() throws BeansException {
		try {
			this.contextOptions = (ContextOptions) getBean(OPTIONS_BEAN_NAME);
		}
		catch (NoSuchBeanDefinitionException ex) {
			logger.info("No options bean '" + OPTIONS_BEAN_NAME + "' found: using default");
			this.contextOptions = new ContextOptions();
		}
	}

	/**
	 * Initialize the message source.
	 * Use parent's if none defined in this context.
	 */
	private void initMessageSource() throws BeansException {
		try {
			this.messageSource = (MessageSource) getBean(MESSAGE_SOURCE_BEAN_NAME);
			// set parent message source if applicable,
			// and if the message source is defined in this context, not in a parent
			if (this.parent != null && (this.messageSource instanceof NestingMessageSource) &&
			    Arrays.asList(getBeanDefinitionNames()).contains(MESSAGE_SOURCE_BEAN_NAME)) {
				((NestingMessageSource) this.messageSource).setParent(this.parent);
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			logger.info("No MessageSource found for [" + getDisplayName() + "]: using empty StaticMessageSource");
			// use empty message source to be able to accept getMessage calls
			this.messageSource = new StaticMessageSource();
		}
	}

	/**
	 * Template method which can be overridden to add context-specific refresh work.
	 * @throws BeansException in case of errors during refresh
	 */
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 */
	private void refreshListeners() throws BeansException {
		logger.info("Refreshing listeners");
		Collection listeners = getBeansOfType(ApplicationListener.class, true, false).values();
		logger.debug("Found " + listeners.size() + " listeners in bean factory");
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			ApplicationListener listener = (ApplicationListener) it.next();
			addListener(listener);
			logger.info("Application listener [" + listener + "] added");
		}
	}

	/**
	 * Destroy the singletons in the bean factory of this application context.
	 */
	public void close() {
		logger.info("Closing application context [" + getDisplayName() + "]");

		// destroy all cached singletons in this context,
		// invoking DisposableBean.destroy and/or "destroy-method"
		getBeanFactory().destroySingletons();

		// publish respective event
		publishEvent(new ContextClosedEvent(this));
	}

	/**
	 * Publish the given event to all listeners.
	 * <p>Note: Listeners get initialized after the message source, to be able
	 * to access it within listener implementations. Thus, message source
	 * implementation cannot publish events.
	 * @param event event to publish. The event may be application-specific,
	 * or a standard framework event.
	 */
	public final void publishEvent(ApplicationEvent event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event in context [" + getDisplayName() + "]: " + event.toString());
		}
		this.eventMulticaster.onApplicationEvent(event);
		if (this.parent != null) {
			parent.publishEvent(event);
		}
	}

	/**
	 * Add a listener. Any beans that are listeners are automatically added.
	 */
	protected void addListener(ApplicationListener l) {
		this.eventMulticaster.addApplicationListener(l);
	}

	/**
	 * This implementation supports fully qualified URLs and appropriate
	 * file paths, via getResourceByPath.
	 * Throws a FileNotFoundException if getResourceByPath returns null.
	 * @see #getResourceByPath
	 */
	public final InputStream getResourceAsStream(String location) throws IOException {
		try {
			// try URL
			URL url = new URL(location);
			logger.debug("Opening as URL: " + location);
			return url.openStream();
		}
		catch (MalformedURLException ex) {
			// no URL -> try (file) path
			InputStream in = getResourceByPath(location);
			if (in == null) {
				throw new FileNotFoundException("Location [" + location + "] could not be opened as file path");
			}
			return in;
		}
	}

	/**
	 * Return input stream to the resource at the given (file) path.
	 * <p>Default implementation supports file paths, either absolute or
	 * relative to the application's working directory. This should be
	 * appropriate for standalone implementations but can be overridden,
	 * e.g. for implementations targeted at a container.
	 * @param path path to the resource
	 * @return InputStream for the specified resource, can be null if
	 * not found (instead of throwing an exception)
	 * @throws IOException exception when opening the specified resource
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		return new FileInputStream(path);
	}

	/**
	 * This implementation returns the working directory of the Java VM.
	 * This should be appropriate for standalone implementations but can
	 * be overridden for implementations targetted at a container.
	 */
	public String getResourceBasePath() {
		return (new File("")).getAbsolutePath() + File.separatorChar;
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource
	//---------------------------------------------------------------------

	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return this.messageSource.getMessage(code, args, defaultMessage, locale);
	}

	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, args, locale);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(resolvable, locale);
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	public Object getBean(String name, Class requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	public boolean containsBean(String name) throws BeansException {
		return getBeanFactory().containsBean(name);
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getAliases(name);
	}

	public void autowireExistingBean(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {
		getBeanFactory().autowireExistingBean(existingBean, autowireMode, dependencyCheck);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public String[] getBeanDefinitionNames(Class type) {
		return getBeanFactory().getBeanDefinitionNames(type);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) throws BeansException {
		return getBeanFactory().getBeansOfType(type, includePrototypes, includeFactoryBeans);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory
	//---------------------------------------------------------------------

	public BeanFactory getParentBeanFactory() {
		return getParent();
	}


	/** Show information about this context */
	public String toString() {
		StringBuffer sb = new StringBuffer("ApplicationContext: displayName=[" + this.displayName + "]; ");
		sb.append("class=[" + getClass().getName() + "]; ");
		sb.append("beanFactory=[" + getBeanFactory() + "]; ");
		sb.append("messageSource=[" + this.messageSource + "]; ");
		sb.append("contextOptions=[" + this.contextOptions + "]; ");
		sb.append("startup date=[" + new Date(this.startupTime) + "]; ");
		if (this.parent == null)
			sb.append("root of ApplicationContext hierarchy");
		else
			sb.append("parent=[" + this.parent + "]");
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by refresh before any other initialization work.
	 * @see #refresh
	 */
	protected abstract void refreshBeanFactory() throws BeansException;

	/**
	 * Subclasses must return their internal bean factory here.
	 * They should implement the lookup efficiently, so that it can be called
	 * repeatedly without a performance penalty.
	 * @return this application context's internal bean factory
	 */
	protected abstract ConfigurableListableBeanFactory getBeanFactory();

}
