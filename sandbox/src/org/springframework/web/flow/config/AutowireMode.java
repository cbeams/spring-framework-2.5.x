/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.config;

import org.springframework.util.enums.support.ShortCodedLabeledEnum;

/**
 * Enumeration listing all possible autowire modes. The autowire mode is
 * used by the <code>FlowServiceLocator</code> when asking the backing
 * registry to create new artifacts, e.g. actions.
 * <p>
 * The autowire modes defined here are inspired by those supported by
 * the Spring <code>AutowireCapableBeanFactory</code>.
 * 
 * @see org.springframework.web.flow.config.FlowServiceLocator
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 *  
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class AutowireMode extends ShortCodedLabeledEnum {

	/**
	 * Constant that indicates autowiring bean properties by name.
	 */
	public static final AutowireMode BY_NAME = new AutowireMode(1, "byName");

	/**
	 * Constant that indicates autowiring bean properties by type.
	 */
	public static final AutowireMode BY_TYPE = new AutowireMode(2, "byType");

	/**
	 * Constant that indicates autowiring a constructor.
	 */
	public static final AutowireMode CONSTRUCTOR = new AutowireMode(3, "constructor");

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 */
	public static final AutowireMode AUTODETECT = new AutowireMode(4, "autodetect");

	/**
	 * Constant that indicates that no autowiring should be done.
	 */
	public static final AutowireMode NONE = new AutowireMode(5, "none");

	/**
	 * Constant that indicates that no explicit autowire mode is specified
	 * and that the default autowire mode of the FlowServiceLocator should
	 * be used.
	 */
	public static final AutowireMode DEFAULT = new AutowireMode(6, "default");

	/**
	 * Private constructor because this is a typesafe enum!
	 */
	private AutowireMode(int code, String label) {
		super(code, label);
	}
}