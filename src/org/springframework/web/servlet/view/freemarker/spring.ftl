<#--
 * spring.ftl
 *
 * This file consists of a collection of FreeMarker macros aimed at easing
 * some of the common requirements of web applications - in particular
 * handling of forms.
 *
 * Spring's FreeMarker support will automatically make this file and therefore
 * all macros within it available to any application using Spring's
 * FreeMarkerConfigurer.
 *
 * To take advantage of these macros, the "exposeSpringMacroHelpers" property
 * of the FreeMarker class needs to be set to "true". This will expose a
 * RequestContext under the name "springMacroRequestContext", as needed by
 * the macros in this library.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 1.1
 -->

<#--
 * spring:bind
 *
 * Exposes a BindStatus object for the given bind path, which can be
 * a bean (e.g. "person") to get global errors, or a bean property
 * (e.g. "person.name") to get field errors. Can be called multiple times
 * within a form to bind to multiple command objects and/or field names.
 *
 * This macro will participate in the default HTML escape setting for the given
 * RequestContext. This can be customized by calling "setDefaultHtmlEscape"
 * on the "springMacroRequestContext" context variable, or via the
 * "defaultHtmlEscape" context-param in web.xml (same as for the JSP bind tag).
 *
 * Producing no output, the following context variable will be available
 * each time this macro is referenced (assuming you import this library in
 * your templates with the namespace 'spring'):
 *
 *   spring.status : a BindStatus instance holding the command object name,
 *   expression, value, and error messages and codes for the path supplied
 *
 * @param path : the path (string value) of the value required to bind to.
 *   Spring defaults to a command name of "command" but this can be overridden
 *   by user config.
 -->
<#macro bind path>
    <#assign status = springMacroRequestContext.getBindStatus(path)>
</#macro>

<#--
 * spring:bindEscaped
 *
 * Similar to spring:bind, but takes an explicit HTML escape flag rather
 * than relying on the default HTML escape setting.
 -->
<#macro bindEscaped path, escape>
    <#assign status = springMacroRequestContext.getBindStatus(path, escape)>
</#macro>

<#--
 * spring:doBind
 * 
 * determine status of html escaping set by the user and call the correct
 * bind macro
 -->
<#macro doBind path >
	<#if htmlEscape?exists>
		<@bindEscaped path, htmlEscape />
	<#else>
		<@bind path/>
	</#if>
</#macro>

<#--
 * formInput
 *
 * display a form input field of type 'text' and bind it to an attribute
 * of a command or bean
 *
 * @param path the name of the field to bind to
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
 *
 -->
<#macro formInput path attributes="" >
    <@doBind path/>
    <input
        type="text"
        name="${spring.status.expression}"
        value="${spring.status.value?default("")}"
        ${attributes}
    <@closeTag/>
</#macro>

<#--
 * formTextArea
 *
 * display a text area and bind it to an attribute
 * of a command or bean
 *
 * @param path the name of the field to bind to
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
 *
 -->
<#macro formTextArea path attributes="" >
    <@doBind path/>
    <textarea
        name="${spring.status.expression}"
        ${attributes}
    >${spring.status.value?default("")}</textarea>
</#macro>

<#--
 * formSingleSelect
 *
 * show a selectbox (dropdown) input element allowing a single value to be chosen
 * from a list of options.
 *
 * @param path the name of the field to bind to
 * @param options a list (sequence) of all the available options
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
-->
<#macro formSingleSelect path options attributes="">
    <@doBind path/>
    <select
        name="${spring.status.expression}"
        ${attributes}>
        <#list options as option>
        <option <#if spring.status.value?default("") == option>selected="true"</#if>>${option}</option>
        </#list>
    </select>
</#macro>

<#--
 * formMultiSelect
 *
 * show a listbox of options allowing the user to make 0 or more choices from 
 * the list of options
 *
 * @param path the name of the field to bind to
 * @param options a list (sequence) of all the available options
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
-->
<#macro formMultiSelect path options attributes="">
    <@doBind path/>
    <select
        multiple="multiple"
        name="${spring.status.expression}"
        ${attributes}>
        <#list options as option>
        <#assign isSelected = contains(spring.status.value?default([""]), option)>
        <option <#if isSelected>selected="true"</#if>>${option}</option>
        </#list>
    </select>
</#macro>

<#--
 * formRadioButtons
 *
 * show radio
 *
 * @param path the name of the field to bind to
 * @param options a list (sequence) of all the available options
 * @param separator the html tag or other character list that should be used to
 *        separate each option.  Typically '&nbsp;' or '<br>'
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
-->
<#macro formRadioButtons path options separator attributes="">
    <@doBind path/>
    <#list options as option>
    <input
        type="radio"
        name="${spring.status.expression}"
        value="${option}"
        <#if spring.status.value?default("") == option>checked="checked"</#if>
        ${attributes}
    <@closeTag/> ${option}${separator}
    </#list>
</#macro>

<#--
 * formCheckBoxes
 *
 * show checkboxes
 *
 * @param path the name of the field to bind to
 * @param options a list (sequence) of all the available options
 * @param separator the html tag or other character list that should be used to
 *        separate each option.  Typically '&nbsp;' or '<br>'
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
-->
<#macro formCheckBoxes path options separator attributes="">
    <@doBind path/>
    <#list options as option>
    <input
        type="checkbox"
        name="${spring.status.expression}"
        value="${option}"
        <#assign isSelected = contains(spring.status.value?default([""]), option)>
        <#if isSelected>checked="checked"</#if>
        ${attributes}
    <@closeTag/> ${option}${separator}
    </#list>
</#macro>

<#--
 * showErrors
 *
 * show validation errors for the currently bound field, with
 * optional style attributes
 *
 * @param separator the html tag or other character list that should be used to
 *        separate each option.  Typically '<br>'
 * @param classOrStyle either the name of a CSS class element (which is defined in
 *        the template or an external CSS file) or an inline style.  If the value passed in here
 *        contains a colon (:) then a 'style=' attribute will be used, else a 'class=' attribute
 *        will be used.
-->
<#macro showErrors separator classOrStyle="">
    <#list spring.status.errorMessages as error>
        <#if classOrStyle == "">
        <b>${error}</b>
        <#else>
            <#if classOrStyle?index_of(":") == -1><#assign attr="class"><#else><#assign attr="style"></#if>
            <span ${attr}="${classOrStyle}">${error}</span>
        </#if>${separator}
    </#list>
</#macro>

<#--
 * listContains
 *
 * macro to return true if the list contains the scalar, false if not.
 * Surprisingly not a freemarker builtin.  This function is used internally but
 * can be accessed by user code if required.
 *
 * @param list the list to search for the item
 * @param item the item to search for in the list
 * @return true if item is found in the list, false otherwise.
-->
<#function contains list item>
    <#list list as nextInList>
        <#if nextInList == item><#return true></#if>
    </#list>
    <#return false>
</#function>

<#--
 * closeTag
 *
 * simple macro to close an HTML tag that has no body  with '>' or '/>'
 * depending on the value of a 'global' variable called 'xhtmlCompliant'
-->
<#macro closeTag>
	<#if xhtmlCompliant?exists && xhtmlCompliant>/><#else>></#if>
</#macro>
