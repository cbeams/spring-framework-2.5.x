/*
 * Created on Jan 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.springframework.context.access;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.JndiBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Subclass of JndiBeanFactoryLocator which creates a
 * ClassPathXmlApplicationContext instead of a BeanFactory.
 * @author Colin Sampaleanu
 * @version $Id: ContextJndiBeanFactoryLocator.java,v 1.1 2004-02-13 17:54:13 jhoeller Exp $
 */
public class ContextJndiBeanFactoryLocator extends JndiBeanFactoryLocator {

	protected BeanFactoryReference createBeanFactory(String[] resources) throws BeansException {
		return new ContextBeanFactoryReference(new ClassPathXmlApplicationContext(resources));
	}

}
