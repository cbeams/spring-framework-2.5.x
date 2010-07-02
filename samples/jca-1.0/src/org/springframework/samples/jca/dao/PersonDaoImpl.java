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
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jca.cci.core.RecordCreator;
import org.springframework.jca.cci.core.RecordExtractor;
import org.springframework.jca.cci.core.support.CciDaoSupport;
import org.springframework.samples.jca.dao.PersonDao;
import org.springframework.samples.jca.exception.PersonException;
import org.springframework.samples.jca.model.Person;

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
	public Person getPerson(final int id) throws PersonException {
		CciInteractionSpec interactionSpec=new CciInteractionSpec();
		/*interactionSpec.setUser("sa");
		interactionSpec.setPassword("");*/
		interactionSpec.setSql("select * from person where person_id=?");
		
		List people=(List)getCciTemplate().execute(interactionSpec,new RecordCreator() {
			public Record createRecord(RecordFactory recordFactory) throws ResourceException, DataAccessException {
				IndexedRecord input=recordFactory.createIndexedRecord("input");
				input.add(new Integer(id));
				return input;
			}
		},new RecordExtractor() {
			public Object extractData(Record record) throws ResourceException, SQLException, DataAccessException {
				List people=new ArrayList();
				ResultSet rs=(ResultSet)record;
				while( rs.next() ) {
					Person person=new Person();
					person.setId(rs.getInt("person_id"));
					person.setLastName(rs.getString("person_last_name"));
					person.setFirstName(rs.getString("person_first_name"));
					people.add(person);
				}
				return people;
			}
		});

		if( people.size()==1 ) {
			return (Person)people.get(0);
		} else {
			throw new PersonException("Can't the person");
		}
	}

	/**
	 * @see org.springframework.samples.jca.dao.PersonDao#updatePerson(org.springframework.samples.jca.model.Person)
	 */
	public void updatePerson(final Person person) {
		StringBuffer request=new StringBuffer();
		request.append("update person set ");
		request.append("person_last_name=?,");
		request.append("person_first_name=?");
		request.append(" where person_id=?");
		CciInteractionSpec interactionSpec=new CciInteractionSpec();
		interactionSpec.setSql(request.toString());
		
		getCciTemplate().execute(interactionSpec,new RecordCreator() {
			public Record createRecord(RecordFactory recordFactory) throws ResourceException, DataAccessException {
				IndexedRecord input=recordFactory.createIndexedRecord("input");
				input.add(person.getLastName());
				input.add(person.getFirstName());
				input.add(new Integer(person.getId()));
				return input;
			}
		},new RecordExtractor() {
			public Object extractData(Record record) throws ResourceException, SQLException, DataAccessException {
				IndexedRecord output=(IndexedRecord)record;
				for(Iterator i=output.iterator();i.hasNext();) {
					log.debug("Number of updated lines : "+i.next());
				}
				return null;
			}
		});
		
	}

}
