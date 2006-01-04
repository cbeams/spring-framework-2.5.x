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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.scripting.AbstractStringBasedScript;
import org.springframework.scripting.ScriptSource;

/**
 * @author Rob Harrop
 * @since 2.0M2
 */
public class JRubyScript extends AbstractStringBasedScript {

	private Class[] interfaces;

	protected JRubyScript(ScriptSource scriptSource, Class[] interfaces) {
		super(scriptSource);
		this.interfaces = interfaces;
	}

	protected Object doCreateObjectFromScript(String script) throws Exception {
		Ruby ruby = Ruby.getDefaultInstance();

		IRubyObject rubyObject = ruby.evalScript(script);
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setInterfaces(this.interfaces);
		proxyFactory.addAdvice(new RubyObjectMethodInterceptor(rubyObject, ruby));

		return proxyFactory.getProxy();
	}

	private static class RubyObjectMethodInterceptor implements MethodInterceptor {

		private IRubyObject rubyObject;

		private Ruby ruby;

		public RubyObjectMethodInterceptor(IRubyObject rubyObject, Ruby ruby) {
			this.rubyObject = rubyObject;
			this.ruby = ruby;
		}

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			String name = methodInvocation.getMethod().getName();
			Object[] javaArgs = methodInvocation.getArguments();

			IRubyObject[] rubyArgs = convertToRuby(javaArgs);
			IRubyObject result = this.rubyObject.callMethod(name, rubyArgs);
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
