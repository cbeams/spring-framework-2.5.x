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

package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

/**
 * Class to retrieve the next value of a given Oracle Sequence.
 * @author Dmitriy Kopylenko
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class OracleSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * Default constructor.
	 **/
	public OracleSequenceMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence/table to use
	 */
	public OracleSequenceMaxValueIncrementer(DataSource ds, String incrementerName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		afterPropertiesSet();
	}

	protected String getSequenceQuery() {
		return "select " + getIncrementerName() + ".nextval from dual";
	}

}
