package org.springframework.scripting.jruby;

import org.jruby.RubyException;
import org.jruby.exceptions.RaiseException;
import org.springframework.core.NestedRuntimeException;

/**
 * Thrown in response to a JRuby {@link RaiseException} being thrown.
 * <p/>
 * <p>Introduced because the <code>RaiseException</code> class does not
 * have useful {@link Object#toString()}, {@link Throwable#getMessage()},
 * and {@link Throwable#printStackTrace} implementations.
 *
 * @author Rick Evans
 * @since 6.0
 */
public class JRubyException extends NestedRuntimeException {

	/**
	 * Create a new instance of the <code>JRubyException</code> class.
	 *
	 * @param exception the cause; must not be <code>null</code>
	 */
	public JRubyException(RaiseException exception) {
		super(createDetailMessage(exception), exception);
	}


	private static String createDetailMessage(RaiseException exception) {
		RubyException rubyEx = exception.getException();
		return (rubyEx != null && rubyEx.message != null)
				? rubyEx.message.toString()
				: "Unexpected JRuby error.";
	}

}
