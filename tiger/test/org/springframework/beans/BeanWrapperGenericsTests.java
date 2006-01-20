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

package org.springframework.beans;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * @author Juergen Hoeller
 * @since 18.01.2006
 */
public class BeanWrapperGenericsTests extends TestCase {

	public void testGenericSet() {
		GenericBean gb = new GenericBean();
		BeanWrapper bw = new BeanWrapperImpl(gb);
		Set input = new HashSet();
		input.add("4");
		input.add("5");
		bw.setPropertyValue("integerSet", input);
		assertTrue(gb.getIntegerSet().contains(new Integer(4)));
		assertTrue(gb.getIntegerSet().contains(new Integer(5)));
	}

	public void testGenericList() throws MalformedURLException {
		GenericBean gb = new GenericBean();
		BeanWrapper bw = new BeanWrapperImpl(gb);
		List input = new ArrayList();
		input.add("http://localhost:8080");
		input.add("http://localhost:9090");
		bw.setPropertyValue("resourceList", input);
		assertEquals(new UrlResource("http://localhost:8080"), gb.getResourceList().get(0));
		assertEquals(new UrlResource("http://localhost:9090"), gb.getResourceList().get(1));
	}

	public void testGenericListElement() throws MalformedURLException {
		GenericBean gb = new GenericBean();
		gb.setResourceList(new ArrayList<Resource>());
		BeanWrapper bw = new BeanWrapperImpl(gb);
		bw.setPropertyValue("resourceList[0]", "http://localhost:8080");
		assertEquals(new UrlResource("http://localhost:8080"), gb.getResourceList().get(0));
	}

	public void testGenericMap() {
		GenericBean gb = new GenericBean();
		BeanWrapper bw = new BeanWrapperImpl(gb);
		Map input = new HashMap();
		input.put("4", "5");
		input.put("6", "7");
		bw.setPropertyValue("shortMap", input);
		assertEquals(new Integer(5), gb.getShortMap().get(new Short("4")));
		assertEquals(new Integer(7), gb.getShortMap().get(new Short("6")));
	}

	public void testGenericMapElement() {
		GenericBean gb = new GenericBean();
		gb.setShortMap(new HashMap<Short,Integer>());
		BeanWrapper bw = new BeanWrapperImpl(gb);
		bw.setPropertyValue("shortMap[4]", "5");
		assertEquals(new Integer(5), gb.getShortMap().get(new Short("4")));
	}

	public void testGenericMapWithKeyTypeOnly() {
		GenericBean gb = new GenericBean();
		BeanWrapper bw = new BeanWrapperImpl(gb);
		Map input = new HashMap();
		input.put("4", "5");
		input.put("6", "7");
		bw.setPropertyValue("longMap", input);
		assertEquals("5", gb.getLongMap().get(new Long("4")));
		assertEquals("7", gb.getLongMap().get(new Long("6")));
	}

	public void testGenericMapElementWithKeyTypeOnly() {
		GenericBean gb = new GenericBean();
		gb.setLongMap(new HashMap<Long,Integer>());
		BeanWrapper bw = new BeanWrapperImpl(gb);
		bw.setPropertyValue("longMap[4]", "5");
		assertEquals("5", gb.getLongMap().get(new Long("4")));
	}

}
