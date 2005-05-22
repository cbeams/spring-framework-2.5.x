package org.springframework.samples.numberguess;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.samples.numberguess.FourDigitNumberGuessAction.NumberGuessData;
import org.springframework.web.flow.Event;

public class FourDigitNumberGuessActionTests extends TestCase {
	public void testGuessNoInputProvided() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setSourceEvent(new Event(this, "submit"));
		FourDigitNumberGuessAction action = new FourDigitNumberGuessAction();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}
	
	public void testGuessInputInvalidLength() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "123");
		context.setSourceEvent(new Event(this, "submit", parameters));
		FourDigitNumberGuessAction action = new FourDigitNumberGuessAction();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputNotAllDigits() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "12AB");
		context.setSourceEvent(new Event(this, "submit", parameters));
		FourDigitNumberGuessAction action = new FourDigitNumberGuessAction();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessInputNotUniqueDigits() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "1111");
		context.setSourceEvent(new Event(this, "submit", parameters));
		FourDigitNumberGuessAction action = new FourDigitNumberGuessAction();
		Event result = action.guess(context);
		assertEquals("invalidInput", result.getId());
	}

	public void testGuessRetry() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("guess", "1234");
		context.setSourceEvent(new Event(this, "submit", parameters));
		FourDigitNumberGuessAction action = new FourDigitNumberGuessAction();
		Event result = action.guess(context);
		assertEquals("retry", result.getId());
	}
	
	public void testGuessCorrect() throws Exception {
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		context.setSourceEvent(new Event(this, "submit", parameters));
		FourDigitNumberGuessAction action = new FourDigitNumberGuessAction();
		Event result = action.guess(context);
		NumberGuessData data = (NumberGuessData)context.getFlowScope().getAttribute("data");
		String answer = data.getAnswer();
		parameters.put("guess", answer);
		context.setSourceEvent(new Event(this, "submit", parameters));
		result = action.guess(context);
		assertEquals("correct", result.getId());
	}
}