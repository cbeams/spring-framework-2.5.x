/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.framework;

/**
 * Interface to be implemented by objects that can create
 * AOP proxies based on AdvisedSupport objects.
 * @author Rod Johnson
 * @version $Id: AopProxyFactory.java,v 1.3 2004-04-30 15:39:01 jhoeller Exp $
 */
public interface AopProxyFactory {
	
	/**
	 * Return an AopProxy for the given AdvisedSupport object.
	 * @param advisedSupport the AOP configuration
	 * @return an AOP proxy
	 * @throws AopConfigException if the configuration is invalid
	 */
	AopProxy createAopProxy(AdvisedSupport advisedSupport) throws AopConfigException;

}
