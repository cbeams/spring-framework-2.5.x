/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

/**
 * {@link DataFieldMaxValueIncrementer} that retrieves the next value of a given HSQL sequence.
 * Thanks to Guillaume Bilodeau for the suggestion!
 *
 * <p><b>NOTE:</b> This is an alternative to using a regular table to support generating
 * unique keys that was necessary in previous versions of HSQL.
 *
 * @author Thomas Risberg
 * @since 2.5
 * @see org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer
 */
public class HsqlSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * Default constructor.
	 **/
	public HsqlSequenceMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence to use
	 */
	public HsqlSequenceMaxValueIncrementer(DataSource ds, String incrementerName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		afterPropertiesSet();
	}

	protected String getSequenceQuery() {
		return "call next value for " + getIncrementerName();
	}

}
