package org.springframework.samples.phonebook.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhoneBook {

	private List persons = new ArrayList();

	public PhoneBook() {
		//setup some test data
		Person jkerry = new Person("John", "Kerry", "jkerry", "11111");
		Person jedwar = new Person("John", "Edwards", "jedwar", "22222");
		Person bclint = new Person("Bill", "Clinton", "bclint", "33333");
		jkerry.addColleague(jedwar);
		jkerry.addColleague(bclint);
		jedwar.addColleague(jkerry);
		jedwar.addColleague(bclint);
		bclint.addColleague(jkerry);
		bclint.addColleague(jedwar);

		Person gwbush = new Person("George Walker", "Bush", "gwbush", "44444");
		Person dchain = new Person("Dick", "Chainey", "dchain", "55555");
		gwbush.addColleague(dchain);
		dchain.addColleague(gwbush);

		persons.add(jkerry);
		persons.add(jedwar);
		persons.add(bclint);
		persons.add(gwbush);
		persons.add(dchain);
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