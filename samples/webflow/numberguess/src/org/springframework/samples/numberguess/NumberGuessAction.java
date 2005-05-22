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
package org.springframework.samples.numberguess;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import org.springframework.binding.convert.support.TextToNumber;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.MultiAction;

/**
 * Action that encapsulates logic for the number guess sample flow.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class NumberGuessAction extends MultiAction {

	public Event guess(RequestContext context) throws Exception {
		NumberGuessData data = getNumberGuessData(context);
		int guess = getGuess(context);
		if (guess < 0 || guess > 100) {
			data.lastGuessResult = "invalid";
			return result("invalidInput");
		}
		else {
			data.guesses++;
			if (data.answer < guess) {
				data.lastGuessResult = "too high!";
				return result("retry");
			}
			else if (data.answer > guess) {
				data.lastGuessResult = "too low!";
				return result("retry");
			}
			else {
				data.lastGuessResult = "correct!";
				Calendar now = Calendar.getInstance();
				long durationMilliseconds = now.getTime().getTime() - data.start.getTime().getTime();
				data.durationSeconds = durationMilliseconds / 1000;
				return success();
			}
		}
	}

	private static final String DATA_ATTRIBUTE = "data";

	private static final String GUESS_PARAMETER = "guess";

	private NumberGuessData getNumberGuessData(RequestContext context) {
		return (NumberGuessData)context.getFlowScope().getOrCreateAttribute(DATA_ATTRIBUTE, NumberGuessData.class);
	}

	private int getGuess(RequestContext context) {
		try {
			return ((Integer)new TextToNumber().convert(
					context.getSourceEvent().getParameter(GUESS_PARAMETER), Integer.class)).intValue();
		}
		catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Simple data holder for number guess info.
	 */
	public static class NumberGuessData implements Serializable {
		
		private static Random random = new Random();

		private Calendar start = Calendar.getInstance();

		private int answer = random.nextInt(101);

		private int guesses = 0;

		private String lastGuessResult = "";

		private long durationSeconds = -1;

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

		public String getLastGuessResult() {
			return lastGuessResult;
		}
	}
}