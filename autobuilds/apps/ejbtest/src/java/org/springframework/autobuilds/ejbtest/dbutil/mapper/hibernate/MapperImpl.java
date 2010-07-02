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

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper;
import org.springframework.autobuilds.ejbtest.dbutil.mapper.MapperFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate implementation of Mapper
 * 
 * @author colin sampaleanu
 */
public class MapperImpl extends MapperHibernateDaoSupport implements Mapper {

  protected MapperFactory _mapperFactory = null;
  
  
  /**
   * Create a Mapper instance
   */
  protected MapperImpl(MapperFactory mapperFactory, SessionFactory sessionFactory) {
    this(sessionFactory);
    setMapperFactory(mapperFactory);
  }

  /**
   * Create a Mapper instance. SessionFactory still needs to be set
   */
  public MapperImpl() {
  }

  /**
   * Create a Mapper instance
   */
  protected MapperImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#getMapperFactory()
   */
  public MapperFactory getMapperFactory() {
    if (_mapperFactory == null)
      throw new IllegalArgumentException("Mapper factory not configured");    

    return _mapperFactory;
  }
  
  /**
   * Sets the MapperFactory associated with this Mapper. <br/>May only be set
   * once. Will throw a RuntimeException if attempted to be set twice
   */
  public void setMapperFactory(MapperFactory mapperFactory) {
    if (_mapperFactory != null)
      throw new RuntimeException("MapperFactory may only be set once");
    _mapperFactory = mapperFactory;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#load(java.lang.Class,
   *      java.io.Serializable)
   */
  public Object load(final Class theClass, final Serializable id) throws DataAccessException {

    return getHibernateTemplate().load(theClass, id);
  }
  
  /* (non-Javadoc)
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.BaseMapper#loadAll(java.lang.Class)
   */
  public List loadAll(Class clazz) throws DataAccessException {
    return getHibernateTemplate().find("from " + clazz.getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#get(java.lang.Class,
   *      java.io.Serializable)
   */
  public Object get(final Class theClass, final Serializable id) throws DataAccessException {

    return getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException {
        return session.get(theClass, id);
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#save(java.lang.Object)
   */
  public Serializable save(final Object object) throws DataAccessException {
    return getHibernateTemplate().save(object);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#saveOrUpdate(java.lang.Object)
   */
  public void saveOrUpdate(final Object object) throws DataAccessException {

    getHibernateTemplate().saveOrUpdate(object);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#update(java.lang.Object,
   *      java.io.Serializable)
   */
  public void update(final Object object, final Serializable id) throws DataAccessException {

    getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException {
        session.update(object, id);
        return null;
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#update(java.lang.Object)
   */
  public void update(final Object object) throws DataAccessException {

    getHibernateTemplate().update(object);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#delete(java.lang.Object)
   */
  public void delete(final Object object) throws DataAccessException {

    getHibernateTemplate().delete(object);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper#flush()
   */
  public void flush() throws DataAccessException {

    getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException {
        session.flush();
        return null;
      }
    });
  }
}
