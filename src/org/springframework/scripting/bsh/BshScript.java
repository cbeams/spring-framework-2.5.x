package org.springframework.scripting.bsh;

import bsh.Interpreter;
import bsh.This;
import bsh.XThis;
import org.springframework.scripting.AbstractScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.aop.framework.ProxyFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Rob Harrop
 */
public class BshScript extends AbstractScript {

	private Class[] interfaces;

	public BshScript(ScriptSource scriptSource, Class[] interfaces) {
		super(scriptSource);
		this.interfaces = interfaces;
	}

	protected Object doCreateObject(InputStream inputStream) throws Exception {
		Interpreter interpreter = new Interpreter();
		interpreter.eval(new InputStreamReader(inputStream));
		XThis xt = (XThis) interpreter.eval("return this");

		ProxyFactory pf = new ProxyFactory();
		pf.setInterfaces(this.interfaces);
		pf.addAdvice(new BshMethodInterceptor(xt));

		return pf.getProxy();
	}

	private static class BshMethodInterceptor implements MethodInterceptor {

		private XThis xt;

		public BshMethodInterceptor(XThis xt) {
			this.xt = xt;
		}

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			return xt.invokeMethod(methodInvocation.getMethod().getName(), methodInvocation.getArguments());
		}
	}
}
