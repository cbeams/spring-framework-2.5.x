/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.export.naming;

import org.springframework.jmx.JmxTestBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Rob Harrop
 */
public class IdentityNamingStrategyTests extends AbstractNamingStrategyTests {

	private JmxTestBean bean = new JmxTestBean();

	protected ObjectNamingStrategy getStrategy() throws Exception {
		return new IdentityNamingStrategy();
	}

	protected Object getManagedResource() throws Exception {
		return bean;
	}

	protected String getKey() {
		return "identity";
	}

	protected String getCorrectObjectName() {
		StringBuffer sb = new StringBuffer(256);

		sb.append(bean.getClass().getPackage().getName());
		sb.append(":");
		sb.append("class=");
		sb.append(ClassUtils.getShortName(bean.getClass()));
		sb.append(",hashCode=");
		sb.append(ObjectUtils.getIdentityHexString(bean));

		return sb.toString();
	}

}
