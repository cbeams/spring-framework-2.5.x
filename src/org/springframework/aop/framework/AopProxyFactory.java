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
 * <p>Proxies should observe the following contract:
 * <ul>
 * <li>They should implement all interfaces that the configuration
 * indicates should be proxied.
 * <li>They should implement the Advised interface
 * <li>They should implement the equals method to compare
 * proxied interfaces, advice, and target
 * <li>They should be serializable if all advisors and target
 * are serializable
 * <li>They should be threadsafe if advisors and target
 * are threadsafe
 * </ul>
 * Proxies may or may not allow advice changes to be made. 
 * If they do not permit advice changes (for example, because
 * the configuration was frozen) a proxy should throw an 
 * AopConfigException on an attempted advice change.
 * @author Rod Johnson
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
