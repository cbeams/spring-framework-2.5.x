<#--
 * spring.ftl
 *
 * This file consists of a collection of FreeMarker macros aimed at easing
 * some of the common requirements of web applications - in particular 
 * handling of forms.
 *
 * Spring's FreeMarker support will automatically make this file and therefore
 * all macros within it available to any application using Spring's 
 * FreeMarkerConfigurer
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 1.1
 -->
 
<#--
 * springBind
 * 
 * Sets some well-known context attributes up to correspond to errors and
 * field values based on the parameters received. Can be called multiple times
 * within a form to bind to new command objects and or field names.
 *
 * Producing no output, the following context variables will be available
 * each time this macro is referenced (assuming you import this library in
 * your templates with the namespace 'spring'):
 *
 *  spring.status : a BindStatus instance holding the command object name,
 *  expression, value, and error messages and codes for the path supplied
 *
 * @param path : the path (string value) of the value required to bind to.
 *   Spring defaults to a command name of "command" but this can be overridden
 *   by user config.
 * 
 * @param escape : should output be HTML escaped (true/false) / default: false
 *
 -->
<#macro bind path, escape=false>
	<#assign status = springMacroRequestContext.createBindStatus(path, escape)>
</#macro>
