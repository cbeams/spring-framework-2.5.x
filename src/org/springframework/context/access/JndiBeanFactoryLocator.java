/*
 * Created on Jan 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.springframework.context.access;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.SimpleJndiBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Subclass of SimpleJndiBeanFactoryLocator which creates an ApplicationContext
 * instead of a BeanFactory
 *
 * @author colin sampaleanu
 * @version $Id: JndiBeanFactoryLocator.java,v 1.1 2004-01-27 00:03:32 colins Exp $
 */
public class JndiBeanFactoryLocator extends SimpleJndiBeanFactoryLocator {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.access.SimpleJndiBeanFactoryLocator#createFactory(java.lang.String[])
	 */
	protected BeanFactory createFactory(String[] resources)
			throws FatalBeanException {

		FileSystemXmlApplicationContext groupContext = new ClassPathXmlApplicationContext(
				resources);

		return groupContext;
	}
}
