/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.ant;

/**
 * A silly attribute that describes a person.  For testing purposes.
 * 
 * @author Mark Pollack
 * @since Oct 6, 2003
 */
public class PersonAttribute {

	/**
	 * The person's name.
	 */
	private String name;

	/**
	 * The person's age.
	 */
	private int age;

	/**
	 * The person's height.
	 */
	private float height;

	/**
	 * A do nothing ctor
	 *
	 */
	public PersonAttribute() {

	}

	/**
	 * Create a PersonAttribute with a name, age and height
	 * @param nome name 
	 * @param idade age
	 * @param altura height
	 */
	public PersonAttribute(String nome, int idade, float altura) {
		name = nome;
		age = idade;
		height = altura;
	}

	/**
	 * Get the name of the person
	 * @return the name of the person.
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * Set the name
	 * @param nome name of the person.
	 */
	public void setName(String nome) {
		name = nome;
	}

	/**
	 * Get the age of the person
	 * @return the age of the person.
	 */
	public int getAge() {
		return this.age;
	}

	/**
	 * Set the age of the person
	 * @param idade the age
	 */
	public void setAge(int idade) {
		age = idade;
	}

	/**
	 * Get the height of the person
	 * @return the height of the person.
	 */
	public float getHeight() {
		return this.height;
	}

	/**
	 * Set the height of the person.
	 * @param altura the height
	 */
	public void setHeight(float altura) {
		height = altura;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb
			.append("Name = ")
			.append(getName())
			.append(" Age = ")
			.append(getAge())
			.append(" Height = ")
			.append(getHeight());
		return sb.toString();
	}
}
