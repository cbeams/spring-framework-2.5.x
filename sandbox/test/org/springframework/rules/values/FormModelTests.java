/*
 * $Header: /usr/local/cvs/module/src/java/File.java,v 1.7 2004/01/16 22:23:11
 * keith Exp $ $Revision: 1.3 $ $Date: 2004-08-26 05:34:10 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import junit.framework.TestCase;

public class FormModelTests extends TestCase {
    public void testNestedProperties() {
        Company c = new Company();
        Contact contact = new Contact();
        contact.setAddress(new Address());
        c.setPrimaryContact(contact);

        ValidatingFormModel model = new ValidatingFormModel(c);
        model.setBufferChangesDefault(false);
        ValueModel city = model.add("primaryContact");
        city.addValueListener(new ValueListener() {
            public void valueChanged() {
                System.out.println("city changed");
            }
        });
        ValueModel addr = model.add("primaryContact.address");
        addr.set(new Address());
    }

    public static class Company {
        private String name;

        private Contact primaryContact;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Contact getPrimaryContact() {
            return primaryContact;
        }

        public void setPrimaryContact(Contact primaryContact) {
            this.primaryContact = primaryContact;
        }
    }

    public static class Contact {
        private String name;

        private Address address;

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            System.out.println("Address set");
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Address {
        private String city;

        private String state;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}