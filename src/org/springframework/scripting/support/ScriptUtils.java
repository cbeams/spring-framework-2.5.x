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

package org.springframework.scripting.support;

import java.beans.PropertyDescriptor;

import net.sf.cglib.asm.Type;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.InterfaceMaker;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility methods for handling scripted Java objects.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class ScriptUtils {

	/**
	 * Create a config interface for the given bean property values.
	 * @param pvs the bean property values to create a config interface for
	 * @param interfaces the interfaces to check against (might define
	 * getters corresponding to the setters we're supposed to generate)
	 * @return the config interface
	 */
	public static Class createConfigInterface(PropertyValue[] pvs, Class[] interfaces) {
		Assert.notEmpty(pvs, "Property values must not be empty");
		InterfaceMaker maker = new InterfaceMaker();
		for (int i = 0; i < pvs.length; i++) {
			String propertyName = pvs[i].getName();
			Class propertyType = findPropertyType(propertyName, interfaces);
			String setterName = "set" + StringUtils.capitalize(propertyName);
			Signature signature = new Signature(setterName, Type.VOID_TYPE, new Type[] {Type.getType(propertyType)});
			maker.add(signature, new Type[0]);
		}
		return maker.create();
	}

	/**
	 * Determine the bean property type for the given property from the
	 * given interfaces, if possible.
	 * @param propertyName the name of the bean property
	 * @param interfaces the interfaces to check against
	 * @return the property type, or <code>Object.class</code> as fallback
	 */
	private static Class findPropertyType(String propertyName, Class[] interfaces) {
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(interfaces[i], propertyName);
				if (pd != null) {
					return pd.getPropertyType();
				}
			}
		}
		return Object.class;
	}

}
