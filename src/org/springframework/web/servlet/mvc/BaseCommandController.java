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

import org.springframework.validation.BindException;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * <p>Controller implementation which creates an object (the command object) on
 * receipt of a request and attempts to populate this object with request parameters.</p>
 *
 * <p>This controller is the base for all controllers wishing to populate
 * JavaBeans based on request parameters, validate the content of such
 * JavaBeans using {@link org.springframework.validation.Validator Validators}
 * and use custom editors (in the form of
 * {@link java.beans.PropertyEditor PropertyEditors}) to transform 
 * objects into strings and vice versa, for example. Three notions are mentioned here:</p>
 *
 * <p><b>Command class:</b><br>
 * An instance of the command class will be created for each request and populated
 * with request parameters. A command class can basically be any Java class; the only
 * requirement is a no-arg constructor. The command class should preferably be a
 * JavaBean in order to be able to populate bean properties with request parameters.</p>
 *
 * <p><b>Populating using request parameters and PropertyEditors:</b><br>
 * Upon receiving a request, any BaseCommandController will attempt to fill the
 * command object using the request parameters. This is done using the typical
 * and well-known JavaBeans property notation. When a request parameter named
 * <code>'firstName'</code> exists, the framework will attempt to call 
 * <code>setFirstName([value])</code> passing the value of the parameter. Nested properties
 * are of course supported. For instance a parameter named <code>'address.city'</code>
 * will result in a <code>getAddress().setCity([value])</code> call on the
 * command class.</p>
 *
 * <p>It's important to realise that you are not limited to String arguments in
 * your JavaBeans. Using the PropertyEditor-notion as supplied by the
 * java.beans package, you will be able to transform Strings to Objects and
 * the other way around. For instance <code>setLocale(Locale loc)</code> is
 * perfectly possible for a request parameter named <code>locale</code> having
 * a value of <code>en</code>, as long as you register the appropriate
 * PropertyEditor in the Controller (see {@link #initBinder initBinder()}
 * for more information on that matter.</p>
 *
 * <p><b>Validators:</b>
 * After the controller has successfully populated the command object with
 * parameters from the request, it will use any configured validators to
 * validate the object. Validation results will be put in a
 * {@link org.springframework.validation.Errors Errors} object which can be
 * used in a View to render any input problems.</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="AbstractController.html#workflow">and that defined by superclass</a>):</b><br>
 * Since this class is an abstract base class for more specific implementation,
 * it does not override the handleRequestInternal() method and also has no
 * actual workflow. Implementing classes like
 * {@link AbstractFormController AbstractFormController},
 * {@link AbstractCommandController AbstractcommandController},
 * {@link SimpleFormController SimpleFormController} and
 * {@link AbstractWizardFormController AbstractWizardFormController}
 * provide actual functionality and workflow.
 * More information on workflow performed by superclasses can be found
 * <a href="AbstractController.html#workflow">here</a>.</p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="AbstractController.html#config">and those defined by superclass</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></th>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>commandName</td>
 *      <td>command</td>
 *      <td>the name to use when binding the instantiated command class
 *          to the request</td>
 *  </tr>
 *  <tr>
 *      <td>commandClass</td>
 *      <td><i>null</i></td>
 *      <td>the class to use upon receiving a request and which to fill
 *          using the request parameters. What object is used and whether
 *          or not it should be created is defined by extending classes
 *          and their configuration properties and methods.</td>
 *  </tr>
 *  <tr>
 *      <td>validator</td>
 *      <td><i>null</i></td>
 *      <td>Validator bean (usually passed in using a &lt;ref bean="beanId"/&gt;
 *          property. The validator will be called at appropriate places in the
 *          workflow of subclasses (have a look at those for more info) to
 *          validate the command object.</td>
 *  </tr>
 *  <tr>
 *      <td>validateOnBinding</td>
 *      <td>true</td>
 *      <td>Indicates whether or not to validate the command object after the
 *          object has been populated with request parameters.</td>
 *  </tr>
 * </table>
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class BaseCommandController extends AbstractController {

	public static final String DEFAULT_COMMAND_NAME = "command";


	private String commandName = DEFAULT_COMMAND_NAME;

	private Class commandClass;

	private Validator[] validators;

	private boolean validateOnBinding = true;

	private MessageCodesResolver messageCodesResolver;


	/**
	 * Set the name of the command in the model.
	 * The command object will be included in the model under this name.
	 */
	public final void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Return the name of the command in the model.
	 */
	public final String getCommandName() {
		return this.commandName;
	}

	/**
	 * Set the command class for this controller.
	 * An instance of this class gets populated and validated on each request.
	 */
	public final void setCommandClass(Class commandClass) {
		this.commandClass = commandClass;
	}

	/**
	 * Return the command class for this controller.
	 */
	public final Class getCommandClass() {
		return this.commandClass;
	}

	/**
	 * Set the primary Validator for this controller.
	 * The Validator must support the specified command class.
	 */
	public final void setValidators(Validator[] validators) {
		this.validators = validators;
	}

	/**
	 * Return the Validators for this controller.
	 */
	public final Validator[] getValidators() {
		return validators;
	}

	/**
	 * Set the Validators for this controller.
	 * The Validator must support the specified command class.
	 */
	public final void setValidator(Validator validator) {
		this.validators = new Validator[] {validator};
	}

	/**
	 * Return the primary Validator for this controller.
	 */
	public final Validator getValidator() {
		return (validators != null && validators.length > 0 ? validators[0] : null);
	}

	/**
	 * Set if the Validator should get applied when binding.
	 */
	public final void setValidateOnBinding(boolean validateOnBinding) {
		this.validateOnBinding = validateOnBinding;
	}

	/**
	 * Return if the Validator should get applied when binding.
	 */
	public final boolean isValidateOnBinding() {
		return validateOnBinding;
	}

	/**
	 * Set the strategy to use for resolving errors into message codes.
	 * Applies the given strategy to all data binders used by this controller.
	 * <p>Default is null, i.e. using the default strategy of the data binder.
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#setMessageCodesResolver
	 */
	public final void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
	}

	/**
	 * Return the strategy to use for resolving errors into message codes.
	 */
	public final MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	protected void initApplicationContext() {
		if (this.validators != null) {
			for (int i = 0; i < this.validators.length; i++) {
				if (this.commandClass != null && !this.validators[i].supports(this.commandClass))
					throw new IllegalArgumentException("Validator [" + this.validators[i] +
																						 "] does not support command class [" +
																						 this.commandClass.getName() + "]");
			}
		}
	}


	/**
	 * Retrieve a command object for the given request.
	 * <p>Default implementation calls createCommand. Subclasses can override this.
	 * @param request current HTTP request
	 * @return object command to bind onto
	 * @see #createCommand
	 */
	protected Object getCommand(HttpServletRequest request) throws Exception {
		return createCommand();
	}

	/**
	 * Create a new command instance for the command class of this controller.
	 * @return the new command instance
	 * @throws InstantiationException if the command class could not be instantiated
	 * @throws IllegalAccessException if the class or its constructor is not accessible
	 */
	protected final Object createCommand() throws InstantiationException, IllegalAccessException {
		if (this.commandClass == null) {
			throw new IllegalStateException("Cannot create command without commandClass being set - " +
																			"either set commandClass or override formBackingObject");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new command of class [" + this.commandClass.getName() + "]");
		}
		return this.commandClass.newInstance();
	}

	/**
	 * Check if the given command object is a valid for this controller,
	 * i.e. its command class.
	 * @param command the command object to check
	 * @return if the command object is valid for this controller
	 */
	protected final boolean checkCommand(Object command) {
		return (this.commandClass == null || this.commandClass.isInstance(command));
	}
	

	/**
	 * Bind the parameters of the given request to the given command object.
	 * @param request current HTTP request
	 * @param command the command to bind onto
	 * @return the ServletRequestDataBinder instance for additional custom validation
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ServletRequestDataBinder bindAndValidate(HttpServletRequest request, Object command)
			throws Exception {
		ServletRequestDataBinder binder = createBinder(request, command);
		binder.bind(request);
		onBind(request, command, binder.getErrors());
		if (this.validators != null && isValidateOnBinding() && !suppressValidation(request)) {
			for (int i = 0; i < this.validators.length; i++) {
				ValidationUtils.invokeValidator(this.validators[i], command, binder.getErrors());
			}
		}
		onBindAndValidate(request, command, binder.getErrors());
		return binder;
	}

	/**
	 * Create a new binder instance for the given command and request.
	 * Called by bindAndValidate. Can be overridden to plug in custom
	 * ServletRequestDataBinder subclasses.
	 * <p>Default implementation creates a standard ServletRequestDataBinder,
	 * sets the specified MessageCodesResolver (if any), and invokes initBinder.
	 * Note that initBinder will not be invoked if you override this method!
	 * @param request current HTTP request
	 * @param command the command to bind onto
	 * @return the new binder instance
	 * @throws Exception in case of invalid state or arguments
	 * @see #bindAndValidate
	 * @see #initBinder
	 * @see #setMessageCodesResolver
	 */
	protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object command)
	    throws Exception {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(command, getCommandName());
		if (this.messageCodesResolver != null) {
			binder.setMessageCodesResolver(this.messageCodesResolver);
		}
		initBinder(request, binder);
		return binder;
	}

	/**
	 * Initialize the given binder instance, for example with custom editors.
	 * Called by createBinder.
	 * <p>This method allows you to register custom editors for certain fields of your
	 * command class. For instance, you will be able to transform Date objects into a
	 * String pattern and back, in order to allow your JavaBeans to have Date properties
	 * and still be able to set and display them in an HTML interface.
	 * <p>Default implementation is empty.
	 * @param request current HTTP request
	 * @param binder new binder instance
	 * @throws Exception in case of invalid state or arguments
	 * @see #createBinder
	 * @see org.springframework.validation.DataBinder#registerCustomEditor
	 * @see org.springframework.beans.propertyeditors.CustomDateEditor
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
	    throws Exception {
	}

	/**
	 * Callback for custom post-processing in terms of binding.
	 * Called on each submit, after standard binding but before validation.
	 * <p>Default implementation delegates to onBind(request, command).
	 * @param request current HTTP request
	 * @param command the command object to perform further binding on
	 * @param errors validation errors holder, allowing for additional
	 * custom registration of binding errors
	 * @throws Exception in case of invalid state or arguments
	 * @see #bindAndValidate
	 * @see #onBind(HttpServletRequest, Object)
	 */
	protected void onBind(HttpServletRequest request, Object command, BindException errors)
			throws Exception {
		onBind(request, command);
	}

	/**
	 * Callback for custom post-processing in terms of binding.
	 * Called by the default implementation of the onBind version with
	 * all parameters, after standard binding but before validation.
	 * <p>Default implementation is empty.
	 * @param request current HTTP request
	 * @param command the command object to perform further binding on
	 * @throws Exception in case of invalid state or arguments
	 * @see #onBind(HttpServletRequest, Object, BindException)
	 */
	protected void onBind(HttpServletRequest request, Object command) throws Exception {
	}

	/**
	 * Return whether to suppress validation for the given request.
	 * Default implementations always returns false.
	 * @param request current HTTP request
	 * @return whether to suppress validation for the given request
	 */
	protected boolean suppressValidation(HttpServletRequest request) {
		return false;
	}

	/**
	 * Callback for custom post-processing in terms of binding and validation.
	 * Called on each submit, after standard binding and validation,
	 * but before error evaluation.
	 * <p>Default implementation is empty.
	 * @param request current HTTP request
	 * @param command the command object, still allowing for further binding
	 * @param errors validation errors holder, allowing for additional
	 * custom validation
	 * @throws Exception in case of invalid state or arguments
	 * @see #bindAndValidate
	 * @see org.springframework.validation.Errors
	 */
	protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors)
			throws Exception {
	}

}
