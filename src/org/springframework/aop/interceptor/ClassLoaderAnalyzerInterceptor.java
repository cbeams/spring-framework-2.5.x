package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassLoaderUtils;

/**
 * Trivial classloader analyzer interceptor.
 * @version $Id: ClassLoaderAnalyzerInterceptor.java,v 1.1.1.1 2003-08-14 16:20:14 trisberg Exp $
 * @author Rod Johnson
 * @author Dmitriy Kopylenko
 */
public class ClassLoaderAnalyzerInterceptor implements MethodInterceptor {

	protected final Log logger = LogFactory.getLog(getClass());

	public Object invoke(MethodInvocation pInvocation) throws Throwable {
		logger.debug("Begin class loader analysis");

		logger.info(ClassLoaderUtils.showClassLoaderHierarchy(
			pInvocation.getThis(),
			pInvocation.getThis().getClass().getName(),
			"\n",
			"-"));
		Object rval = pInvocation.proceed();

		logger.debug("End class loader analysis");
		return rval;
	}

}
