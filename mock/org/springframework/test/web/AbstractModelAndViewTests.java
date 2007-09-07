/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.test.web;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.web.servlet.ModelAndView;

/**
 * Convenient base class for tests dealing with Spring web MVC
 * {@link org.springframework.web.servlet.ModelAndView} objects.
 *
 * @author Alef Arendsen
 * @author Bram Smeets
 * @since 2.0
 * @see org.springframework.web.servlet.ModelAndView
 */
public abstract class AbstractModelAndViewTests extends TestCase {

	/**
	 * Assert whether or not a model attribute is available.
	 */
	protected void assertModelAttributeAvailable(ModelAndView mav, Object key) {
		assertNotNull("Model is null", mav.getModel());
		assertTrue("Model attribute with name '" + key + "' is not available",
				mav.getModel().containsKey(key));
	}	

	/**
	 * Compare each individual entry in a list, not sorting the lists first.
	 */
	protected void assertCompareListModelAttribute(ModelAndView mav, Object key, List assertionList) {
		List modelList = (List) assertAndReturnModelAttributeOfType(mav, key, List.class);
		assertEquals("Size of model list is '" + modelList.size() +
				"' while size of assertion list is '" + assertionList.size() + "'",
				assertionList.size(), modelList.size());		
		assertEquals("List in model under key '" + key + "' is not equals to given list", assertionList, modelList);
	}

	/**
	 * Compare each individual entry in a list after having sorted both lists
	 * (optionally using a comparator).
	 * @param comp the comparator to use. If not specifying the comparator,
	 * both lists will be sorted not using any comparator.
	 */
	protected void assertSortAndCompareListModelAttribute(
			ModelAndView mav, Object key, List assertionList, Comparator comp) {

		List modelList = (List) assertAndReturnModelAttributeOfType(mav, key, List.class);
		assertEquals("Size of model list is '" + modelList.size() +
				"' while size of assertion list is '" + assertionList.size() + "'",
				assertionList.size(), modelList.size());

		if (comp != null) {
			Collections.sort(modelList, comp);
			Collections.sort(assertionList, comp);
		}
		else {
			Collections.sort(modelList);
			Collections.sort(assertionList);
		}		
		assertEquals("List in model under key '" + key + "' is not equals to given list", assertionList, modelList);
	}

	/**
	 * Checks whether the given model key exists and checks it type, based
	 * on the given type. If the model entry exists and the type matches,
	 * the given model value is returned.
	 */
	protected Object assertAndReturnModelAttributeOfType(ModelAndView mav, Object key, Class type) {
		assertNotNull("Model is null", mav.getModel());
		assertNotNull("Model attribute with key '" + key + "' is null", mav.getModel().get(key));
		Object obj = mav.getModel().get(key);
		assertTrue("Model attribute is not of type '" + type.getName() + "' but is a '" +
				obj.getClass().getName() + "'", type.isAssignableFrom(obj.getClass()));
		return obj;
	}
	
	/**
	 * Check to see if the view name in the ModelAndView matches the given String.
	 */
	protected void assertViewName(ModelAndView mav, String name) {
		assertEquals("View name is not equal to '" + name + "' but was '" + mav.getViewName() + "'",
				name, mav.getViewName());
	}

	/**
	 * Compare a given Object to the value from the model bound under the given key.
	 */
	protected void assertModelAttributeValue(ModelAndView mav, Object key, Object value) {
		Object modelValue = assertAndReturnModelAttributeOfType(mav, key, Object.class);
		assertEquals("Model value with key '" + key + "' is not the same as given value which was '" + value + "'", 
				value, modelValue);				
	}

	/**
	 * Inspect the given model to see if all elements in the model appear and are equal
	 */
	protected void assertModelAttributeValues(ModelAndView mav, Map assertionModel) {
		assertNotNull(mav.getModel());
		
		if (!mav.getModel().keySet().equals(assertionModel.keySet())) {
			StringBuffer buf = new StringBuffer("Keyset of given model does not match.\n");
			appendNonMatchingSetsErrorMessage(assertionModel.keySet(), mav.getModel().keySet(), buf);
			fail(buf.toString());
		}
		
		StringBuffer buf = new StringBuffer();
		Iterator it = mav.getModel().keySet().iterator();
		while (it.hasNext()) {
			Object key = (Object) it.next();
			Object assertionValue = assertionModel.get(key);
			Object mavValue = mav.getModel().get(key);
			if (!assertionValue.equals(mavValue)) {
				buf.append("Value under key '" + key + "' differs, should have been '" +
						assertionValue + "' but was '" + mavValue +"'\n");
			}
		}

		if (buf.length() != 0) {
			 buf.insert(0, "Values of given model do not match.\n");
			 fail(buf.toString());
		}
	}

	private void appendNonMatchingSetsErrorMessage(Set assertionSet, Set incorrectSet, StringBuffer buf) {
		Set tempSet = new HashSet();
		tempSet.addAll(incorrectSet);
		tempSet.removeAll(assertionSet);
		
		if (tempSet.size() > 0) {
			buf.append("Set has too many elements:\n");
			Iterator it = tempSet.iterator();
			while (it.hasNext()) {
				Object o = (Object) it.next();
				buf.append('-');
				buf.append(o.toString());
				buf.append('\n');
			}
		}
		
		tempSet = new HashSet();
		tempSet.addAll(assertionSet);
		tempSet.removeAll(incorrectSet);
		
		if (tempSet.size() > 0) {
			buf.append("Set is missing elements:\n");
			Iterator it = tempSet.iterator();
			while (it.hasNext()) {
				Object o = (Object) it.next();
				buf.append('-');
				buf.append(o.toString());
				buf.append('\n');
			}
		}	 
	}
	
}
