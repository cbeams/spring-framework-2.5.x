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
import org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper;
import org.springframework.autobuilds.ejbtest.domain.User1;

/**
 * A POJO implementation of SimpleService. This is wrapped transactionally for some
 * tests. This would be the case when using Spring transactions only, or CMT+Spring
 * transactions. On the other hand, if using CMT alone (with no Spring transactions)
 * this would not be wrapped at all.
 * 
 * @author colin sampaleanu
 */
public class MapperUsingSimpleService implements SimpleService {
	
	Log log = LogFactory.getLog(DelegatingSimpleServiceImpl.class);
	
	Mapper mapper;
	
	/**
	 * @param mapper The mapper to set.
	 */
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	/* (non-Javadoc)
	 * @see org.springframework.autobuilds.ejbtest.simple.SimpleService#echo(java.lang.String)
	 */
	public String echo(String input) {
		log.debug("MapperUsingSimpleService:echo");
		return "(MapperUsingSimpleService:echo: hello " + input + ")";
	}

	/* (non-Javadoc)
	 * @see org.springframework.autobuilds.ejbtest.simple.SimpleService#echo2(java.lang.String)
	 */
	public String echo2(String input) {
		log.debug("MapperUsingSimpleService:echo");
		return "(MapperUsingSimpleService:echo2: hello " + input + ")";
	}

	/* (non-Javadoc)
	 * @see org.springframework.autobuilds.ejbtest.simple.SimpleService#echo3(java.lang.String)
	 */
	public String echo3(String input) {
		
		log.debug("MapperUsingSimpleService:echo");

		User1 user = new User1(null, "joe", "password");
		mapper.save(user);
		
		return "(MapperUsingSimpleService:echo3: hello " + input + ")";
	}
}
