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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.factory.BeanFactory;

/**
 * Interface responsible for creating instances corresponding
 * to a root bean definition. This is pulled out into
 * a strategy as various approaches are possible, including
 * using CGLIB to create subclasses on the fly to support
 * Method Injection.
 * @author Rod Johnson
 * @version $Id: InstantiationStrategy.java,v 1.1 2004-06-23 21:08:56 johnsonr Exp $
 */
public interface InstantiationStrategy {
	
	Object instantiate(RootBeanDefinition rbd, BeanFactory owner);
	
	Object instantiate(RootBeanDefinition rbd, BeanFactory owner, Constructor ctor, Object[] args);
	
	Object instantiate(RootBeanDefinition rbd, BeanFactory owner, Method factoryMethod, Object[] args);
	
}