package org.springframework.aop;

import org.springframework.aop.BeforeAdvice;

/**
 * Simple BeforeAdvice targeted for testing
 * @author Dmitriy Kopylenko
 * @version $Id: SimpleBeforeAdvice.java,v 1.1 2004-02-27 14:28:16 dkopylenko Exp $
 */
public interface SimpleBeforeAdvice extends BeforeAdvice {
	
	void before() throws Throwable;

}
