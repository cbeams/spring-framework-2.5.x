/*
 * Copyright (c) 2003 JTeam B.V.
 * www.jteam.nl
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JTeam B.V. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with JTeam.
 */
package org.springframework.web.servlet.tags;

import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class TagUtilsTestSuite extends TestCase {
	
	public TagUtilsTestSuite(String name) {
		super(name);		
	}
	
	public void testTagUtils() {
		// it's simple, test all scope, plus a non-existing one
		// (which should evaluate to PAGE)
		
		assertEquals(TagUtils.PAGE, "page");
		assertEquals(TagUtils.APPLICATION, "application");
		assertEquals(TagUtils.SESSION, "session");
		assertEquals(TagUtils.REQUEST, "request");
		
		assertEquals(TagUtils.getScope("page"), PageContext.PAGE_SCOPE);
		assertEquals(TagUtils.getScope("request"), PageContext.REQUEST_SCOPE);
		assertEquals(TagUtils.getScope("session"), PageContext.SESSION_SCOPE);
		assertEquals(TagUtils.getScope("application"), PageContext.APPLICATION_SCOPE);
		assertEquals(TagUtils.getScope("bla"), PageContext.PAGE_SCOPE);
		
		try {
			TagUtils.getScope(null);
			fail("Null scope, no excpetion thrown!");			
		} catch (IllegalArgumentException e) {
			// ok
		}
	}

}
