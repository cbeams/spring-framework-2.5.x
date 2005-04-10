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

package org.springframework.samples.jca.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jca.cci.core.CciDaoSupport;
import org.springframework.jca.cci.core.CciSpecsHolder;
import org.springframework.jca.cci.core.DefaultCciSpecsHolder;
import org.springframework.jca.cci.core.DefaultRecordGeneratorFromFactory;
import org.springframework.jca.cci.core.RecordExtractor;
import org.springframework.jca.cci.core.RecordGeneratorFromFactory;
import org.springframework.samples.jca.exception.PersonException;
import org.springframework.samples.jca.model.Person;

import com.sun.connector.cciblackbox.CciConnectionSpec;
import com.sun.connector.cciblackbox.CciInteractionSpec;

/**
 * 
 * 
 * @author Thierry TEMPLIER
 */
public class PersonDaoImpl extends CciDaoSupport implements PersonDao {

	private static final Log log = LogFactory.getLog(PersonDaoImpl.class);

	/**
	 * @see org.springframework.samples.jca.dao.PersonneDao#getPerson(int)
	 */
	public Person getPerson(int id) throws PersonException {
		ConnectionSpec cSpec=new CciConnectionSpec();
		InteractionSpec iSpec=new CciInteractionSpec();
		CciSpecsHolder specs=new DefaultCciSpecsHolder(cSpec,iSpec) {
			public void initSpecs(ConnectionSpec connectionSpec,InteractionSpec interactionSpec) throws ResourceException {
				((CciConnectionSpec)connectionSpec).setUser("sa");
				((CciConnectionSpec)connectionSpec).setPassword("");
				((CciInteractionSpec)interactionSpec).setSql("select * from person where person_id=?");
			}
		};
		
		List people=(List)getCciTemplate().execute(specs,new Integer(id),new DefaultRecordGeneratorFromFactory() {
			public Record generateRecord(Object object) throws ResourceException,DataAccessException {
				IndexedRecord input=createIndexedRecord("input");
				input.add((Integer)object);
				return input;
			}
		},new RecordExtractor() {
			public Object extractData(Record rc) throws ResourceException,SQLException,DataAccessException {
				List people=new ArrayList();
				ResultSet rs=(ResultSet)rc;
				while( rs.next() ) {
					Person person=new Person();
					person.setId(rs.getInt("person_id"));
					person.setLastName(rs.getString("person_last_name"));
					person.setFirstName(rs.getString("person_first_name"));
					people.add(person);
				}
				return people;
			}
		},false);

		if( people.size()==1 ) {
			return (Person)people.get(0);
		} else {
			throw new PersonException("Can't the person");
		}
	}

	/**
	 * @see org.springframework.samples.jca.dao.PersonDao#updatePerson(org.springframework.samples.jca.model.Person)
	 */
	public void updatePerson(Person person) {
		ConnectionSpec cSpec=new CciConnectionSpec();
		InteractionSpec iSpec=new CciInteractionSpec();
		CciSpecsHolder specs=new DefaultCciSpecsHolder(cSpec,iSpec) {
			public void initSpecs(ConnectionSpec connectionSpec,InteractionSpec interactionSpec) throws ResourceException {
				((CciConnectionSpec)connectionSpec).setUser("sa");
				((CciConnectionSpec)connectionSpec).setPassword("");
				StringBuffer request=new StringBuffer();
				request.append("update person set ");
				request.append("person_last_name=?,");
				request.append("person_first_name=?");
				request.append(" where person_id=?");
				((CciInteractionSpec)interactionSpec).setSql(request.toString());
			}
		};
		
		getCciTemplate().execute(specs,person,new DefaultRecordGeneratorFromFactory() {
			public Record generateRecord(Object object) throws ResourceException,DataAccessException {
				Person person=(Person)object;
				IndexedRecord input=getCciTemplate().createIndexedRecord("input");
				input.add(person.getLastName());
				input.add(person.getFirstName());
				input.add(new Integer(person.getId()));
				return input;
			}
		},new RecordExtractor() {
			public Object extractData(Record rc) throws ResourceException,SQLException,DataAccessException {
				IndexedRecord output=(IndexedRecord)rc;
				for(Iterator i=output.iterator();i.hasNext();) {
					log.debug("Number of updated lines : "+i.next());
					System.out.println();
				}
				return null;
			}
		},false);
	}

}
