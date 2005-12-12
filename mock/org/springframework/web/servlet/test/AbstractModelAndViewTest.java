package org.springframework.web.servlet.test;

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
 * Convenience super class for classes dealing with
 * and Spring MVC ModelAndView objects.
 * 
 * @author Alef Arendsen
 * @author Bram Smeets
 * @since 2.0
 */
public class AbstractModelAndViewTest extends TestCase {
	
	/**
	 * Asserts whether or not a model attribute is available 
	 */
	protected void assertModelAttributeAvailable(ModelAndView mav, Object key) {
		assertNotNull("Model is null", mav.getModel());
		assertTrue("Model attribute with name '" + key + "' is not available",
				mav.getModel().containsKey(key));
	}	
	
	/**
	 * Compares each individual entry in a list, not sorting the lists first.
	 */
	protected void assertCompareListModelAttribute(ModelAndView mav, Object key, List assertionList) {
		
		List modelList = (List)assertAndReturnModelAttributeOfType(mav, key, List.class);
		
		assertEquals("Size of model list is '" + modelList.size() + 
				"' while size of assertion list is '" + assertionList.size() + "'",
				assertionList.size(), modelList.size());		
		
		assertEquals("List in model under key '" + key + "' is not equals to given list", assertionList, modelList);
		
	}
	
	/**
	 * Compares each individual entry in a list after
	 * having sorted both lists (optionally using a comparator)
	 * @param comp if not specifying the comparator, both lists will be sorted
	 * not using any comparator.
	 */
	protected void assertSortAndCompareListModelAttribute(ModelAndView mav, Object key, List assertionList, Comparator comp) {
		
		List modelList = (List)assertAndReturnModelAttributeOfType(mav, key, List.class);
		
		assertEquals("Size of model list is '" + modelList.size() + 
				"' while size of assertion list is '" + assertionList.size() + "'",
				assertionList.size(), modelList.size());
		
		if (comp != null) {
			Collections.sort(modelList, comp);
			Collections.sort(assertionList, comp);
		} else {
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
		assertNotNull("Model attribute with key '" + key + "' is null", 
				mav.getModel().get(key));
		
		Object o = mav.getModel().get(key);
		
		assertTrue("Model attribute is not of type '" + type.getName() + "' but is a '" + o.getClass().getName() + "'", 
				type.isAssignableFrom(o.getClass()));
		
		return o;
	}
	
	/**
	 * Checks to see if the view name in the ModelAndView matches the
	 * given String.
	 */
	protected void assertViewName(ModelAndView mav, String name) {
		assertEquals("View name is not equal to '" + name + "' but was '" + mav.getViewName() + "'",
				name, mav.getViewName());
	}
	
	/**
	 * Compares a given Object to the value from the model bound under the given key. 
	 */
	protected void assertModelAttributeValue(ModelAndView mav, Object key, Object value) {
		Object modelValue = assertAndReturnModelAttributeOfType(mav, key, Object.class);
		assertEquals("Model value with key '" + key + "' is not the same as given value which was '" + value + "'", 
				value, modelValue);				
	}
	
	/**
	 * Inspects the given model to see if all elements in the model appear and are equal 
	 */
	protected void assertModelAttributeValues(ModelAndView mav, Map assertionModel) {
		assertNotNull(mav.getModel());
		
		if (!mav.getModel().keySet().equals(assertionModel.keySet())) {
			StringBuffer buffy = new StringBuffer("Keyset of given model does not match.\n");
			appendNonMatchingSetsErrorMessage(assertionModel.keySet(), mav.getModel().keySet(), buffy);
			fail(buffy.toString());			
		}
		
		StringBuffer buffy = new StringBuffer();
		Iterator it = mav.getModel().keySet().iterator();
		while (it.hasNext()) {
			Object key = (Object) it.next();
			Object assertionValue = assertionModel.get(key);
			Object mavValue = mav.getModel().get(key);
			if (!assertionValue.equals(mavValue)) {
				buffy.append("Value under key '" + key + "' differs, should have been '" + assertionValue + "' but was '" + mavValue +"'\n");
			}
		}

		if (buffy.length() != 0) {
			 buffy.insert(0, "Values of given model do not match.\n");
			 fail(buffy.toString());
		}
	}

	private void appendNonMatchingSetsErrorMessage(Set assertionSet, Set incorrectSet, StringBuffer buffy) {
		
		// first check for extra values in mavModel assertionModel
		Set tempSet = new HashSet();
		tempSet.addAll(incorrectSet);
		tempSet.removeAll(assertionSet);
		
		if (tempSet.size() > 0) {
			buffy.append("Set has too many elements:\n");
			Iterator it = tempSet.iterator();
			while (it.hasNext()) {
				Object o = (Object) it.next();
				buffy.append('-');
				buffy.append(o.toString());
				buffy.append('\n');
			}
		}
		
		tempSet = new HashSet();
		tempSet.addAll(assertionSet);
		tempSet.removeAll(incorrectSet);
		
		if (tempSet.size() > 0) {
			buffy.append("Set is missing elements:\n");
			Iterator it = tempSet.iterator();
			while (it.hasNext()) {
				Object o = (Object) it.next();
				buffy.append('-');
				buffy.append(o.toString());
				buffy.append('\n');
			}
		}	 
	}
	
}
