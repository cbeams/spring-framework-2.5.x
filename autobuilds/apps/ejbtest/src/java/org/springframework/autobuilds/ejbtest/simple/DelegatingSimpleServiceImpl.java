/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.autobuilds.ejbtest.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A POJO implementation of SimpleService
 */
public class DelegatingSimpleServiceImpl implements SimpleService {
	
	Log log = LogFactory.getLog(DelegatingSimpleServiceImpl.class);

	private SimpleService delegate;
	
	/**
	 * @param delegate The delegate to set.
	 * @todo Generated comment
	 */
	public void setDelegate(SimpleService delegate) {
		this.delegate = delegate;
	}

	/* (non-Javadoc)
	 * @see org.springframework.autobuilds.ejbtest.simple.SimpleService#echo(java.lang.String)
	 */
	public String echo(String input) {
		log.debug("DelegatingSimpleServiceImpl:echo");
		return "(DelegatingSimpleServiceImpl:echo: hello " + input + ")";
	}

	/* (non-Javadoc)
	 * @see org.springframework.autobuilds.ejbtest.simple.SimpleService#echo2(java.lang.String)
	 */
	public String echo2(String input) {
		log.debug("DelegatingSimpleServiceImpl:echo2");
		return "(DelegatingSimpleServiceImpl:echo2: hello " + input + ")";
	}

	/* (non-Javadoc)
	 * @see org.springframework.autobuilds.ejbtest.simple.SimpleService#echo3(java.lang.String)
	 */
	public String echo3(String input) {
		log.debug("DelegatingSimpleServiceImpl:echo3");
		return delegate.echo3(input);
	}
}
