package org.springframework.samples.petclinic.aspects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Aspect to illustrate Spring native load-time weaver.
 * 
 * @author Ramnivas Laddad
 * @since 2.5
 */
@Aspect
public abstract class AbstractTraceAspect {
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Pointcut
	public abstract void traced();
	
	@Before("traced()")
	public void trace(JoinPoint.StaticPart jpsp) {
		logger.debug("Entering " + jpsp.getSignature().toLongString());
	}
}
