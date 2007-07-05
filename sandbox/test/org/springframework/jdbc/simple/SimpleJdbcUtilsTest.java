/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.simple;

import junit.framework.TestCase;
import org.springframework.jdbc.core.simple.SimpleJdbcUtils;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Unit tests for SimpleJdbcUtils.
 *
 * @author Thomas Risberg
 */
public class SimpleJdbcUtilsTest extends TestCase {

	public void testcommonDatabaseName() {
		assertEquals("Wrong db name", "Oracle", SimpleJdbcUtils.commonDatabaseName("Oracle"));
		assertEquals("Wrong db name", "DB2", SimpleJdbcUtils.commonDatabaseName("DB2-for-Spring"));
		assertEquals("Wrong db name", "Sybase", SimpleJdbcUtils.commonDatabaseName("Sybase SQL Server"));
		assertEquals("Wrong db name", "Sybase", SimpleJdbcUtils.commonDatabaseName("Adaptive Server Enterprise"));
		assertEquals("Wrong db name", "MySQL", SimpleJdbcUtils.commonDatabaseName("MySQL"));
	}

	public void testConvertUnderscoreNameToPropertyName() {
		assertEquals("Wrong property name", "myName", SimpleJdbcUtils.convertUnderscoreNameToPropertyName("MY_NAME"));
		assertEquals("Wrong property name", "yourName", SimpleJdbcUtils.convertUnderscoreNameToPropertyName("yOUR_nAME"));
		assertEquals("Wrong property name", "AName", SimpleJdbcUtils.convertUnderscoreNameToPropertyName("a_name"));
		assertEquals("Wrong property name", "someoneElsesName", SimpleJdbcUtils.convertUnderscoreNameToPropertyName("someone_elses_name"));
	}

}
