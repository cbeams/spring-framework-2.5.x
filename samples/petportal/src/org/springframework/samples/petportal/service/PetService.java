/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.samples.petportal.service;

import java.util.Date;
import java.util.SortedSet;

import org.springframework.samples.petportal.domain.Pet;

/**
 * The PetService interface.
 * 
 * @author John A. Lewis
 * @author Mark Fisher
 */
public interface PetService {
	
	public final static String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";

	public Pet getPet(Integer key);

	public Pet getPet(int key);

	public SortedSet getAllPets();

	public int addPet(Pet pet);

	public int addPet(String species, String breed, String name, Date birthdate);

	public void savePet(Pet pet);

	public void deletePet(Integer key);

	public void deletePet(Pet pet);

	public void deletePet(int key);
	
}