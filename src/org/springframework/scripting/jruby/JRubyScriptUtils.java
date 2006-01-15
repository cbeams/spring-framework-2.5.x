/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.scripting.jruby;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jruby.Ruby;
import org.jruby.exceptions.JumpException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import org.springframework.util.ClassUtils;

/**
 * Utility methods for handling JRuby-scripted objects.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class JRubyScriptUtils {

	/**
	 * Create a new JRuby-scripted object from the given script source.
	 * @param scriptSource the script source text
	 * @param interfaces the interfaces that the scripted Java object
	 * is supposed to implement
	 * @return the scripted Java object
	 * @throws JumpException in case of JRuby parsing failure
	 */
	public static Object createJRubyObject(String scriptSource, Class[] interfaces) throws JumpException {
		Ruby ruby = Ruby.getDefaultInstance();
		IRubyObject rubyObject = ruby.evalScript(scriptSource);
		return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
				interfaces, new RubyObjectInvocationHandler(rubyObject, ruby));
	}


	/**
	 * InvocationHandler that invokes a JRuby script method.
	 */
	private static class RubyObjectInvocationHandler implements InvocationHandler {

		private final IRubyObject rubyObject;

		private final Ruby ruby;

		public RubyObjectInvocationHandler(IRubyObject rubyObject, Ruby ruby) {
			this.rubyObject = rubyObject;
			this.ruby = ruby;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			IRubyObject[] rubyArgs = convertToRuby(args);
			IRubyObject result = this.rubyObject.callMethod(method.getName(), rubyArgs);
			return JavaUtil.convertRubyToJava(result);
		}

		private IRubyObject[] convertToRuby(Object[] javaArgs) {
			if (javaArgs == null || javaArgs.length == 0) {
				return new IRubyObject[0];
			}
			return JavaUtil.convertJavaArrayToRuby(this.ruby, javaArgs);
		}
	}

}
