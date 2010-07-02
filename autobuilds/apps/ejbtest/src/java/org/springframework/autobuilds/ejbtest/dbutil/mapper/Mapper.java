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

import java.io.Serializable;
import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * An interface for abstracting persistence related operations. It maps a set
 * of domain objects to a datasource without either knowing of each other.
 *
 * This interface is generally tailored towards fairly transparent O/R mapping
 * such as Hibernate or JDO. Users can for example expect the loading of an
 * entire objects graph, with lazy semantics to ensure adequate performance.
 * 
 * @author colin sampaleanu
 */
public interface Mapper {
  
  /**
   * Return the persistent instance of the given entity class with the given identifier.<br>
   * <br>
   * You should not use this method to determine if an instance exists
   * (use {@link #get(Class theClass, Serializable id)} or a finder method instead). Use this
   * only to retrieve an instance that you assume exists, where non-existence would be an actual
   * error.
   *
   * @param theClass a persistent class
   * @param id a valid identifier of an existing persistent instance of the class
   * @return the persistent instance
   * @throws DataAccessException if the specified instance does not exist, or there is a data
   * access error
   */
  public Object load(Class theClass, Serializable id) throws DataAccessException;

  /**
   * Return all persistent instances of the given entity class, as a List.<br>
   *
   * @param clazz a persistent class  
   * @return a List containing 0 or more persistent instances
   * @throws DataAccessException if there is a data access error
   */
  public List loadAll(Class clazz) throws DataAccessException;
  
  /**
   * Return the persistent instance of the given entity class with the given identifier,
   * if it exists, returns null otherwise.<br>
   * <br>
   *
   * @param theClass a persistent class
   * @param id a valid identifier of an existing persistent instance of the class
   * @return the persistent instance
   * @throws DataAccessException if there is a data access error
   */
  public Object get(Class theClass, Serializable id) throws DataAccessException;
  
  /**
   * Persist the given transient instance, first assigning a generated identifier. (Or
   * using the current value of the identifier property if the <tt>assigned</tt>
   * generator is used.)
   *
   * @param object a transient instance of a persistent class
   * @return the generated identifier
   * @throws DataAccessException
   */
  public Serializable save(Object object) throws DataAccessException;
  
  /**
   * Either <tt>save()</tt> or <tt>update()</tt> the given instance, depending upon the value of
   * its identifier property. By default the instance is always saved. This behaviour may be
   * adjusted by configuring the mapper (in an implementation specific fashion).
   *
   * @see #save(java.lang.Object)
   * @see #update(Object object, Serializable id)
   * @param object a transient instance containing new or updated state
   * @throws DataAccessException
   */
  public void saveOrUpdate(Object object) throws DataAccessException;
  
  /**
   * Update the persistent instance with the identifier of the given transient
   * instance. If there is a persistent instance with the same identifier,
   * an exception is thrown.<br>
   * <br>
   * If the given transient instance has a <tt>null</tt> identifier, an exception
   * will be thrown.<br>
   * <br>
   *
   * @param object a transient instance containing updated state
   * @throws DataAccessException
   */
  public void update(Object object) throws DataAccessException;
  
  /**
   * Update the persistent state associated with the given identifier. An exception
   * is thrown if there is a persistent instance with the same identifier in the
   * current session.<br>
   * <br>
   *
   * @param object a transient instance containing updated state
   * @param id identifier of persistent instance
   * @throws DataAccessException
   */
  public void update(Object object, Serializable id) throws DataAccessException;
  
  /**
   * Remove a persistent instance from the datastore. The argument may be
   * an instance associated with the receiving <tt>Session</tt> or a transient
   * instance with an identifier associated with existing persistent state.
   *
   * @param object the instance to be removed
   * @throws DataAccessException
   */
  public void delete(Object object) throws DataAccessException;
  
  /**
   * Force the Mapper Session to flush. Must be called at the end of a
   * unit of work, before commiting the transaction and closing the
   * session, however committing a transaction calls this method). <i>Flushing</i>
   * is the process of synchronising the underlying persistent store with
   * persistable state held in memory.
   *
   * @throws DataAccessException
   */
  public void flush() throws DataAccessException;
  
  /**
   * returns a MapperFactory instance related to this Mapper (possibly the MapperFactory
   * used to create this mapper), which may be used to get other, domain object specfic
   * Mappers
   * @return a suitable MapperFactory instance
   */
  public MapperFactory getMapperFactory();
  
}
