/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/test/org/springframework/binding/value/support/ValueHolderTests.java,v 1.1 2004-10-28 22:17:50 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-10-28 22:17:50 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.binding.value.support;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.binding.value.ValueModel;
import org.springframework.util.closure.Closure;

/**
 * 
 * @author HP
 */
public class ValueHolderTests extends TestCase {

	public void testRefreshableValueHolder() {
		final List list = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		Closure schemaAccessor = new Closure() {
			public Object call(Object o) {
				System.out.println("Adding");
				list.add(new Integer(list.size() + 1));
				return list;
			}
		};
		ValueModel itemsValueModel = new RefreshableValueHolder(schemaAccessor, true);
		assertEquals(new Integer(4), ((List)itemsValueModel.getValue()).get(3));
	}
}