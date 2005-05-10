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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.MultiAction;

/**
 * Action that encapsulates logic for the number guess sample flow.
 * 
 * @author Keri Donald
 * @author Keith Donald
 */
public class FourDigitNumberGuessAction extends MultiAction {

	public Event guess(RequestContext context) throws Exception {
		NumberGuessData data = getNumberGuessData(context);
		String guess = getGuess(context);
		if (guess == null || guess.length() != 4) {
			return result("invalidInput");
		}
		for (int i = 0; i < 4; i++) {
			if (!Character.isDigit(guess.charAt(i))) {
				return result("invalidInput");
			}
			int digit = Character.getNumericValue(guess.charAt(i));
			for (int j = 0; j < i; j++) {
				if (digit == Character.getNumericValue(guess.charAt(j))) {
					return result("invalidInput");
				}
			}
		}
		int rightPosition = 0;
		int correctButWrongPosition = 0;
		for (int i = 0; i < guess.length(); i++) {
			char digit = guess.charAt(i);
			for (int j = 0; j < data.answer.length(); j++) {
				char answerDigit = data.answer.charAt(j);
				if (digit == answerDigit) {
					if (i == j) {
						rightPosition++;
					} else {
						correctButWrongPosition++;
					}
					break;
				}
			}
		}
		data.recordGuessData(guess, rightPosition, correctButWrongPosition);
		if (rightPosition == 4) {
			Calendar now = Calendar.getInstance();
			long durationMilliseconds = now.getTime().getTime() - data.start.getTime().getTime();
			data.durationSeconds = durationMilliseconds / 1000;
			return result("correct");
		} else {
			return result("retry");
		}
	}

	private static final String DATA_ATTRIBUTE = "data";

	private static final String GUESS_PARAMETER = "guess";

	private NumberGuessData getNumberGuessData(RequestContext context) {
		return (NumberGuessData)context.getFlowScope().getOrCreateAttribute(DATA_ATTRIBUTE, NumberGuessData.class);
	}

	private String getGuess(RequestContext context) {
		return (String)context.getSourceEvent().getParameter(GUESS_PARAMETER);
	}

	/**
	 * Simple data holder for number guess info.
	 */
	public static class NumberGuessData implements Serializable {
		
		private static Random random = new Random();

		private Calendar start = Calendar.getInstance();

		private String answer;

		private List guessHistory = new ArrayList();
		
		private long durationSeconds = -1;

		// property accessors for JSTL EL

		public NumberGuessData() {
			this.answer = createAnswer();
		}
		
		public void recordGuessData(String guess, int rightPosition, int correctButWrongPosition) {
			guessHistory.add(new GuessData(guess, rightPosition, correctButWrongPosition));
		}
		
		public String createAnswer() {
			StringBuffer buffer = new StringBuffer(4);
			for (int i = 0; i < 4; i++) {
				int digit = random.nextInt(10);
				for (int j = 0; j < i; j++) {
					if (digit == Character.getNumericValue(buffer.charAt(j))) {
						j = -1;
						digit = random.nextInt(10);
					}
				}
				buffer.append(digit);
			}
			return buffer.toString();
		}
		
		public String getAnswer() {
			return answer;
		}

		public long getDurationSeconds() {
			return durationSeconds;
		}

		public int getGuesses() {
			return guessHistory.size();
		}

		public Collection getGuessHistory() {
			return guessHistory;
		}
		
		public GuessData getLastGuessData() {
			if (guessHistory.isEmpty()) {
				return null;
			}
			return (GuessData)guessHistory.get(guessHistory.size() - 1);
		}
		
		public class GuessData {
			private String guess;
			
			private int rightPosition;
			
			private int correctButWrongPosition;

			public GuessData(String guess, int rightPosition, int correctButWrongPosition) {
				this.guess = guess;
				this.rightPosition = rightPosition;
				this.correctButWrongPosition = correctButWrongPosition;
			}
			
			public int getCorrectButWrongPosition() {
				return correctButWrongPosition;
			}

			public String getGuess() {
				return guess;
			}

			public int getRightPosition() {
				return rightPosition;
			}	
		}
	}	
}