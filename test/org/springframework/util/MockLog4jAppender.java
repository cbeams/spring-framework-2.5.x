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
package org.springframework.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Alef Arendsen
 */
public class MockLog4jAppender extends AppenderSkeleton {
	
	public static final List loggingStrings = new ArrayList();
	
	public static boolean closeCalled = false;
	
	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	protected void append(LoggingEvent evt) {
		System.out.println("Adding " + evt.getMessage());
		loggingStrings.add(evt.getMessage());
	}
	
	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#close()
	 */
	public void close() {
		closeCalled = true;
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	public boolean requiresLayout() {		
		return false;
	}
	
	


}
