<#--
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
 *
 -->
 
<#-- ------------------------------------------------------------------------ -->

<#--
 *
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
 * @since 1.1
 * @version $Id: spring.ftl,v 1.1 2004-07-02 00:40:02 davison Exp $
 -->
 
<#-- ------------------------------------------------------------------------ -->

<#--
 * springBind
 * 
 * sets some well-known context attributes up to correspond to errors
 * and field values based on the parameters received.  Can be called 
 * multiple times within a form to bind to new command objects and or
 * field names.
 *
 * Producing no output, the following context variables will be available
 * each time this macro is referenced (assuming you import this library in
 * your templates with the namespace 'spring'):
 *
 *  spring.status : a BindStatus instance holding the command object name,
 *  expression, value and error messages and codes for the path supplied
 *
 * @param path the path (string value) of the value required to bind to.  Spring
 *   defaults to a command name of "command" but this can be overridden by user 
 *   config.
 * 
 * @param escape : should output be HTML escaped (true/false) / default: false
 *
 -->
<#macro bind path, escape=false>
	<#assign status = springBindStatusHelper.createBindStatus(path, escape)>
</#macro>
