
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

package org.springframework.jca.cci.core;

import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.cci.Record;

import org.springframework.dao.DataAccessException;

/** 
 * Callback interface used by CciTemplate's query methods.
 * Implementations of this interface perform the actual work of extracting
 * results, but don't need to worry about exception handling. ResourceExceptions
 * will be caught and handled correctly by the CciTemplate class.
 *
 * @author thierry TEMPLIER
 */
public interface RecordExtractor {
	
	/** 
	 * Implementations must implement this method to process
	 * all datas in the Record.
	 * @param rc Record to extract data from.
	 * @return an arbitrary result object, or null if none
	 * (the extractor will typically be stateful in the latter case).
	 * @throws ResourceException if a ResourceException is encountered getting data
	 * @throws DataAccessException in case of custom exceptions
	 */
	public Object extractData(Record rc) throws ResourceException,SQLException,DataAccessException;

}
