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

package org.springframework.autobuilds.ejbtest.dbutil.mapper;

/**
 * Interface which defines a mapper factory
 * 
 * @author colin sampaleanu
 */
public interface MapperFactory {
  
  /**
   * Returns an object implementing the root Mapper interface
   * @return a Mapper instance 
   */
  Mapper getMapper();
  
  /**
   * Returns a mapper/DAO object implementing the specified interface.
   * 
   * @param rootMapper the root mapper object
   * @param mapperClass the interface of the specific mapper to return
   * @return an instance of the specified mapper
   * @throws IllegalArgumentException if the specified mapper is not known to the factory
   */
  Object getMapper(Class mapperClass) throws IllegalArgumentException;

}
