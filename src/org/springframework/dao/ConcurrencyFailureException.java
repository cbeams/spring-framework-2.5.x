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

package org.springframework.dao;

/**
 * Exception thrown on aconcurrency failure.  This exception should be sublassed to 
 * indicate the type of failure - optimistic locking, failur to acquire lock etc.
 * This exception will be thrown either by O/R mapping tools or by custom DAO implementations.
 * @author Thomas Risberg
 * @since 30.06.2004
 */
public class ConcurrencyFailureException extends DataAccessException {

	public ConcurrencyFailureException(String msg) {
		super(msg);
	}

	public ConcurrencyFailureException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
