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

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Convenient TargetSourceCreator using bean name prefixes to create one of three
 * well-known TargetSource types: 
 * <li>: CommonsPoolTargetSource
 * <li>% ThreadLocalTargetSource
 * <li>! PrototypeTargetSource
 * 
 * @author Rod Johnson
 * @version $Id: QuickTargetSourceCreator.java,v 1.3 2004-03-18 02:46:16 trisberg Exp $
 */
public class QuickTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	protected final AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		if (beanName.startsWith(":")) {
			CommonsPoolTargetSource cpts = new CommonsPoolTargetSource();
			cpts.setMaxSize(25);
			return cpts;
			
		}
		else if (beanName.startsWith("%")) {
			ThreadLocalTargetSource tlts = new ThreadLocalTargetSource();
			return tlts;
		}
		else if (beanName.startsWith("!")) {
			PrototypeTargetSource pts = new PrototypeTargetSource();
			return pts;
		}
		else {
			// No match. Don't create a custom target source.
			return null;
		}
	}
	
}