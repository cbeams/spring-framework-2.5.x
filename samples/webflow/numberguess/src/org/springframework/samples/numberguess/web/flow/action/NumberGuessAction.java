/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.samples.numberguess.web.flow.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.MultiAction;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

public class NumberGuessAction extends MultiAction {
	
	private static final String DATA_ATTRIBUTE = "data";
	private static final String GUESS_PARAMETER = "guess";
	
	private static Random random = new Random(); 

	/**
	 * Simple data holder.
	 */
	public static class NumberGuessData implements Serializable {
		public Calendar start = Calendar.getInstance();
		public int answer = random.nextInt(101);
		public int guesses = 0;
		public String indication = "";
		public long durationSeconds = -1;
		
		// property accessors for JSTL EL
		
		public int getAnswer() {
			return answer;
		}
		
		public long getDurationSeconds() {
			return durationSeconds;
		}
		
		public int getGuesses() {
			return guesses;
		}
		
		public String getIndication() {
			return indication;
		}
	}
	
	private NumberGuessData getNumberGuessData(RequestContext context) {
		if (!context.getFlowScope().containsAttribute(DATA_ATTRIBUTE)) {
			context.getFlowScope().setAttribute(DATA_ATTRIBUTE, new NumberGuessData());
		}
		return (NumberGuessData)context.getFlowScope().getAttribute(DATA_ATTRIBUTE);
	}
	
	private int getGuess(RequestContext context) {
		HttpServletRequest request=((HttpServletRequestEvent)context.getOriginatingEvent()).getRequest();
		try {
			return Integer.parseInt(request.getParameter(GUESS_PARAMETER));
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public Event guess(RequestContext context) throws Exception {
		NumberGuessData data = getNumberGuessData(context);
		int guess = getGuess(context);
		if (guess < 0 || guess > 100) {
			data.indication = "invalid";
			return result("invalidInput");
		}
		else {
			data.guesses++;
			
			if (data.answer < guess) {
				data.indication = "lower";
				return result("retry");
			}
			else if (data.answer > guess) {
				data.indication = "higher";
				return result("retry");
			}
			else {
				data.indication = "correct";
		        Calendar now = Calendar.getInstance(); 
		        long durationMilliseconds = now.getTime().getTime()-data.start.getTime().getTime(); 
		        data.durationSeconds = durationMilliseconds / 1000;
		        return success();
			}
		}
	}

}
