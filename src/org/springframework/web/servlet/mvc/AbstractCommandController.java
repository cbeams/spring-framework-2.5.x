/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Abstract base class for custom command controllers. Autopopulates a
 * command bean from the request. For command validation, a validator
 * (property inherited from BaseCommandController) can be used.</p>
 *
 * <p>This command controller should preferrable not be used to handle form
 * submission, because functionality for forms is more offered in more
 * detail by the {@link org.springframework.web.servlet.mvc.AbstractFormController
 * AbstractFormController} and its corresponding implementations.</p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="AbstractController.html#config">and those defined by superclass</a>):</b><br>
 * <i>none</i> (so only those available in superclass).</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a name="BaseCommandController.html#workflow">and that defined by superclass</a>):</b><br>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setCommandClass
 * @see #setCommandName
 * @see #setValidator
 */
public abstract class AbstractCommandController extends BaseCommandController {

	/**
	 * Create a new AbstractCommandController.
	 */
	public AbstractCommandController() {
	}

	/**
	 * Create a new AbstractCommandController.
	 * @param commandClass class of the command bean
	 */
	public AbstractCommandController(Class commandClass) {
		setCommandClass(commandClass);
	}

	/**
	 * Create a new AbstractCommandController.
	 * @param commandClass class of the command bean
	 * @param commandName name of the command bean
	 */
	public AbstractCommandController(Class commandClass, String commandName) {
		setCommandClass(commandClass);
		setCommandName(commandName);
	}
	
	protected final ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Object command = getCommand(request);
		ServletRequestDataBinder binder = bindAndValidate(request, command);
		return handle(request, response, command, binder.getErrors());
	}

	/**
	 * Template method for request handling, providing a populated and validated instance
	 * of the command class, and an Errors object containing binding and validation errors.
	 * <p>Call <code>errors.getModel()</code> to populate the ModelAndView model
	 * with the command and the Errors instance, under the specified command name,
	 * as expected by the "spring:bind" tag.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param command the populated command object
	 * @param errors validation errors holder
	 * @return a ModelAndView to render, or null if handled directly
	 * @see org.springframework.validation.Errors
	 * @see org.springframework.validation.BindException#getModel
	 */
	protected abstract ModelAndView handle(
			HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
			throws Exception;

}
