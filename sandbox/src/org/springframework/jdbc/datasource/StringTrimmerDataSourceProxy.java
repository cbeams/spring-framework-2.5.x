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

package org.springframework.jdbc.datasource;

import java.lang.reflect.Method;
import java.sql.ResultSet;

import org.springframework.util.StringUtils;

/**
 * Proxy for a target DataSource, adding trimming of String results returned
 * by ResultSet's <code>getString</code> and <code>getObject</code> methods.
 * @author Juergen Hoeller
 * @since 29.01.2005
 */
public class StringTrimmerDataSourceProxy extends GenericDataSourceProxy {

	protected Object handleResultSetInvocation(ResultSet target, Method method, Object[] args) throws Throwable {
		Object retVal = super.handleResultSetInvocation(target, method, args);
		if ("getString".equals(method.getName()) ||
				("getObject".equals(method.getName()) && retVal instanceof String)) {
			return StringUtils.trimTrailingWhitespace((String) retVal);
		}
		return retVal;
	}

}
