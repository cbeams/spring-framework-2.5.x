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
package org.springframework.samples.phonebook.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhoneBook {

	private List persons = new ArrayList();

	public PhoneBook() {
		// setup some test data
		Person kd = new Person("Keith", "Donald", "kdonald", "11111");
		Person ev = new Person("Erwin", "Vervaet", "klr8", "22222");
		Person cs = new Person("Colin", "Sampaleanu", "sampa", "33333");
		Person jh = new Person("Juergen", "Hoeller", "jhoeller", "44444");
		Person rj = new Person("Rod", "Johnson", "rod", "55555");
		Person tr = new Person("Thomas", "Risberg", "trisberg", "66666");
		Person aa = new Person("Alef", "Andersen", "alef", "77777");
		Person mp = new Person("Mark", "Pollack", "mark", "888888");
		
		kd.addColleague(ev);
		kd.addColleague(cs);
		kd.addColleague(jh);
		kd.addColleague(rj);
		kd.addColleague(tr);
		kd.addColleague(aa);
		kd.addColleague(mp);
		
		ev.addColleague(kd);
		ev.addColleague(cs);
		ev.addColleague(jh);
		ev.addColleague(rj);
		
		cs.addColleague(kd);
		cs.addColleague(ev);
		cs.addColleague(jh);
		cs.addColleague(rj);
		cs.addColleague(aa);
		cs.addColleague(mp);

		rj.addColleague(cs);
		rj.addColleague(kd);
		rj.addColleague(ev);
		rj.addColleague(jh);
		rj.addColleague(tr);
		rj.addColleague(aa);
		rj.addColleague(mp);

		jh.addColleague(cs);
		jh.addColleague(kd);
		jh.addColleague(ev);
		jh.addColleague(jh);
		jh.addColleague(tr);
		jh.addColleague(aa);

		Person sa = new Person("Shaun", "Alexander", "rolltide", "44444");
		Person dj = new Person("Darell", "Jackson", "gatorcountry", "55555");
		sa.addColleague(dj);
		dj.addColleague(sa);

		persons.add(kd);
		persons.add(ev);
		persons.add(cs);
		persons.add(jh);
		persons.add(rj);
		persons.add(tr);
		persons.add(aa);
		persons.add(mp);

		persons.add(sa);
		persons.add(dj);
	}

	public List query(PhoneBookQuery query) {
		List res = new ArrayList();
		Iterator it = persons.iterator();
		while (it.hasNext()) {
			Person person = (Person)it.next();
			if ((person.getFirstName().indexOf(query.getFirstName()) != -1)
					&& (person.getLastName().indexOf(query.getLastName()) != -1)) {
				res.add(person);
			}
		}
		return res;
	}

	public Person getPerson(UserId userId) {
		Iterator it = persons.iterator();
		while (it.hasNext()) {
			Person person = (Person)it.next();
			if (userId.equals(person.getUserId())) {
				return person;
			}
		}
		return null;
	}
}