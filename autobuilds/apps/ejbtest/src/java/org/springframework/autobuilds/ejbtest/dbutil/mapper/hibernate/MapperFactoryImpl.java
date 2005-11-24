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

package org.springframework.autobuilds.ejbtest.dbutil.mapper.hibernate;

import java.util.HashMap;

import org.hibernate.SessionFactory;

import org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper;
import org.springframework.autobuilds.ejbtest.dbutil.mapper.MapperFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * An implementation of MapperFactory. An instance of this should either be
 * obtained by the client from the container (IOC), or through the root mapper
 * 
 * @author colin sampaleanu
 */
public class MapperFactoryImpl implements MapperFactory {

  // --- attributes
  SessionFactory _sessionFactory;

  protected Mapper _rootMapper;
  protected HashMap _mappers;

  public MapperFactoryImpl() {
  }

  public MapperFactoryImpl(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  /**
   * Sets the mappers this factory supports. Each array element is an array of objects,
   * with the first element in that array being the mapper object itself, and the 2nd and
   * any optional subsequent elements being either the interface classes or string
   * representation of the interface classes supported by that mapper.
   *  
   * @param mappers the mappers, as described above
   * @throws IllegalArgumentException if more than one mapper implements the same mapper
   * interface
   * 
   * @todo Generated comment
   */
  public void setMappers(Object[][] mappers) throws IllegalArgumentException {
    if (_mappers != null)
      throw new IllegalArgumentException(
          "Illegal to set mappers supported by factory more than once");

    _mappers = new HashMap();

    for (int i = 0; i < mappers.length; ++i) {
      Object[] mapperInfo = mappers[i];
      Object mapper = mapperInfo[0];
      if (mapperInfo.length < 2)
        throw new InvalidDataAccessApiUsageException(
            "Mapper specification does not include at least the mapper object and one interface supported by the mapper");
      for (int iintf = 1; iintf < mapperInfo.length; ++iintf) {
        Object intDesc = mapperInfo[iintf];
        Class iface = null;
        try {
          if (intDesc instanceof String)
            iface = Class.forName((String) intDesc, true, mapper.getClass()
                .getClassLoader());
          else
            iface = (Class) intDesc;
        }
        catch (Exception e) {
          throw new InvalidDataAccessApiUsageException(
              "Unable to establish at least one interface for specified mapper object",
              e);
        }

        Object existing = _mappers.get(iface);
        if (existing != null)
          throw new InvalidDataAccessApiUsageException("Trying to register mapper '"
              + mapper.getClass().getName() + "' as implementing interface '"
              + iface.getName()
              + "' but that interface already mapped to another mapper");

        _mappers.put(iface, mapper);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.MapperFactory#getMapper(org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper, java.lang.Class)
   */
  public Object getMapper(Class mapperClass)
      throws IllegalArgumentException {

    Object mapper = _mappers.get(mapperClass);
    if (mapper == null)
      throw new IllegalArgumentException("Specified mapper, '"
          + mapperClass.getName() + "' not supported by this mapper factory");

    return mapper;
  }

  /**
   * Sets the SessionFactory instance, for bean-style use
   * 
   * @param sessionFactory
   */
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  /**
   * @return Returns the root Mapper.
   */
  public Mapper getMapper() {
    return _rootMapper;
  }
  
  /**
   * @param rootMapper The root Mapper to set.
   */
  public void setRootMapper(Mapper rootMapper) {
    _rootMapper = rootMapper;
    ((MapperImpl)_rootMapper).setMapperFactory(this);
  }
}
