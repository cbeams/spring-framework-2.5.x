package org.springframework.scripting.support;

import org.springframework.scripting.Messenger;

/**
 * @author Rick Evans
 */
public final class StubMessenger implements Messenger {

	public static final String MESSAGE = "I used to be smart... now I'm just stupid.";

	public String getMessage() {
		return MESSAGE;
	}
}
