/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.servlet.view;

import java.util.List;
import java.util.Map;

import org.springframework.beans.TestBean;

/**
 * Dummy request context used for VTL and FTL macro tests.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 25.01.2005
 * @see org.springframework.web.servlet.support.RequestContext
 */
public class DummyMacroRequestContext {

	private TestBean command;

	private Map msgMap;

	private String contextPath;


	public void setCommand(TestBean command) {
		this.command = command;
	}

	public TestBean getCommand() {
		return this.command;
	}

	public void setMsgMap(Map msgMap) {
		this.msgMap = msgMap;
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getMessage(String)
	 */
	public String getMessage(String code) {
		return (String) this.msgMap.get(code);
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getMessage(String, String)
	 */
	public String getMessage(String code, String defaultMsg) {
		String msg = (String) this.msgMap.get(code);
		return (msg != null ? msg : defaultMsg);
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getMessage(String, List)
	 */
	public String getMessage(String code, List args) {
		return ((String) this.msgMap.get(code)) + args.toString();
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getMessage(String, List, String)
	 */
	public String getMessage(String code, List args, String defaultMsg) {
		String msg = (String) this.msgMap.get(code);
		return (msg != null ? msg  + args.toString(): defaultMsg);
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getContextPath()
	 */
	public String getContextPath() {
		return this.contextPath;
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getBindStatus(String)
	 */
	public DummyBindStatus getBindStatus(String path) throws IllegalStateException {
		return new DummyBindStatus();
	}

	/**
	 * @see org.springframework.web.servlet.support.RequestContext#getBindStatus(String, boolean)
	 */
	public DummyBindStatus getBindStatus(String path, boolean htmlEscape) throws IllegalStateException {
		return new DummyBindStatus();
	}


	public static class DummyBindStatus {

		public String getExpression() {
			return "name";
		}

		public String getValue() {
			return "Darren";
		}
	}

}
